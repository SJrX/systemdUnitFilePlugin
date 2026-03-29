import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Internal
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.ls.DOMImplementationLS
import org.w3c.dom.ls.LSParser
import org.w3c.dom.ls.LSSerializer
import org.xml.sax.InputSource

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Matcher

/**
 * This task scans the systemd source code man pages to extract the set of available options as well as (eventually) documentation.
 *
 * The logic in this class relies on some structuring assumptions on the source XML, namely
 * that all options are under a varListEntry:
 *
 * refentry > refsect1 > variablelist (with attribute class='unit-directives') > varlistentry
 *
 * The title from the varlistentry is:
 *
 * varlistentry > term > varname
 *
 * The section that it applies to is (two levels up) back up to the refsect1:
 *
 * .. > .. > title/text()
 *
 * The title is a free from string, many files only have one section, but some have two. We (conceptually)
 * maintain a map from the (filename, section) -> the actual section that option should appear in.
 *
 *
 */
class GenerateDataFromManPages extends DefaultTask {

  @InputDirectory
  File systemdSourceCodeRoot

  @OutputDirectory
  File generatedJsonFileLocation

  /**
   * Map that stores for each file name, the name of an option attribute
   */
  @Internal
  def fileTypeToFileAndSectionTitleToSectionName = [
    'unit'  : [
      'systemd.unit.xml'            :
        ['sections':
           ['[Unit] Section Options'   : ['Unit'],
            '[Install] Section Options': ['Install'],
            'Conditions and Asserts'   : ['Unit']
           ]
        ],
      'systemd.service.xml'         :
        ['sections':
           ['Options': ['Service']]
        ],
      'systemd.slice.xml':
      ['sections':
        ['Options': ['Slice']]
    ],
    'systemd.timer.xml'           :
        ['sections':
           ['Options': ['Timer']]
        ],
      'systemd.automount.xml'       :
        ['sections':
           ['Options': ['Automount']]
        ],
      'systemd.mount.xml'           :
        ['sections':
           ['Options': ['Mount']]
        ],
      'systemd.path.xml'            :
        ['sections':
           ['Options': ['Path']]
        ],
      'systemd.socket.xml'          :
        ['sections':
           ['Options': ['Socket']]
        ],
      'systemd.swap.xml'            :
        ['sections':
           ['Options': ['Swap']]
        ],
      'systemd.resource-control.xml':
        ['sections':
           [
             'Options'           : ['Slice', 'Service', 'Socket', 'Mount', 'Swap'],
             'Deprecated Options': ['Slice', 'Service', 'Socket', 'Mount', 'Swap'],
           ]
        ],
      'systemd.kill.xml'            :
        ['sections':
           ['Options': ['Service', "Socket", "Mount", "Swap"]]
        ],
      'systemd.exec.xml'            :
        ['sections':
           [
             'Paths'                            : ['Service', 'Socket', 'Mount', 'Swap'],
             'Credentials'                      : ['Service', 'Socket', 'Mount', 'Swap'],
             'User/Group Identity'              : ['Service', 'Socket', 'Mount', 'Swap'],
             'Capabilities'                     : ['Service', 'Socket', 'Mount', 'Swap'],
             'Security'                         : ['Service', 'Socket', 'Mount', 'Swap'],
             'Mandatory Access Control'         : ['Service', 'Socket', 'Mount', 'Swap'],
             'Process Properties'               : ['Service', 'Socket', 'Mount', 'Swap'],
             'Scheduling'                       : ['Service', 'Socket', 'Mount', 'Swap'],
             'Sandboxing'                       : ['Service', 'Socket', 'Mount', 'Swap'],
             'System Call Filtering'            : ['Service', 'Socket', 'Mount', 'Swap'],
             'Environment'                      : ['Service', 'Socket', 'Mount', 'Swap'],
             'Logging and Standard Input/Output': ['Service', 'Socket', 'Mount', 'Swap'],
             'System V Compatibility'           : ['Service', 'Socket', 'Mount', 'Swap'],
           ]
        ]],
    'nspawn': [
      'systemd.nspawn.xml':
        ['sections':
           [
             '[Exec] Section Options'   : ['Exec'],
             '[Files] Section Options'  : ['Files'],
             '[Network] Section Options': ['Network'],
           ]
        ]],
    //cat systemd.netdev.xml  | grep "Section Options"  | sed -e "s/<title>//g" | sed -e 's#</title>##g' | sed -E "s/\[(.+)] Section Options/'\0': ['\1'],/"
    'netdev': [
      'systemd.netdev.xml':
        ['sections':
          [
            '[Match] Section Options': ['Match'],
            '[NetDev] Section Options': ['NetDev'],
            '[Bridge] Section Options': ['Bridge'],
            '[VLAN] Section Options': ['VLAN'],
            '[MACVLAN] Section Options': ['MACVLAN'],
            '[MACVTAP] Section Options': ['MACVTAP'],
            '[IPVLAN] Section Options': ['IPVLAN'],
            '[IPVTAP] Section Options': ['IPVTAP'],
            '[VXLAN] Section Options': ['VXLAN'],
            '[GENEVE] Section Options': ['GENEVE'],
            '[BareUDP] Section Options': ['BareUDP'],
            '[L2TP] Section Options': ['L2TP'],
            '[L2TPSession] Section Options': ['L2TPSession'],
            '[MACsec] Section Options': ['MACsec'],
            '[MACsecReceiveChannel] Section Options': ['MACsecReceiveChannel'],
            '[MACsecTransmitAssociation] Section Options': ['MACsecTransmitAssociation'],
            '[MACsecReceiveAssociation] Section Options': ['MACsecReceiveAssociation'],
            '[Tunnel] Section Options': ['Tunnel'],
            '[FooOverUDP] Section Options': ['FooOverUDP'],
            '[Peer] Section Options': ['Peer'],
            '[VXCAN] Section Options': ['VXCAN'],
            '[Tun] Section Options': ['Tun'],
            '[Tap] Section Options': ['Tap'],
            '[WireGuard] Section Options': ['WireGuard'],
            '[WireGuardPeer] Section Options': ['WireGuardPeer'],
            '[Bond] Section Options': ['Bond'],
            '[Xfrm] Section Options': ['Xfrm'],
            '[VRF] Section Options': ['VRF'],
            '[BatmanAdvanced] Section Options': ['BatmanAdvanced'],
            '[IPoIB] Section Options': ['IPoIB'],
            '[WLAN] Section Options': ['WLAN'],
          ]
        ]
      ],
    'network': [
      'systemd.network.xml':
      ['sections':
        [
          '[Match] Section Options' : ['Match'],
          '[Link] Section Options' : ['Link'],
          '[SR-IOV] Section Options' : ['SR-IOV'],
          '[Network] Section Options' : ['Network'],
          '[Address] Section Options' : ['Address'],
          '[Neighbor] Section Options': ['Neighbor'],
          '[IPv6AddressLabel] Section Options': ['IPv6AddressLabel'],
          '[RoutingPolicyRule] Section Options': ['RoutingPolicyRule'],
          '[NextHop] Section Options': ['NextHop'],
          '[Route] Section Options': ['Route'],
          '[DHCPv4] Section Options': ['DHCPv4'],
          '[DHCPv6] Section Options': ['DHCPv6'],
          '[DHCPPrefixDelegation] Section Options': ['DHCPPrefixDelegation'],
          '[IPv6AcceptRA] Section Options': ['IPv6AcceptRA'],
          '[DHCPServer] Section Options': ['DHCPServer'],
          '[DHCPServerStaticLease] Section Options': ['DHCPServerStaticLease'],
          '[IPv6SendRA] Section Options': ['IPv6SendRA'],
          '[IPv6Prefix] Section Options': ['IPv6Prefix'],
          '[IPv6RoutePrefix] Section Options': ['IPv6RoutePrefix'],
          '[IPv6PREF64Prefix] Section Options': ['IPv6PREF64Prefix'],
          '[Bridge] Section Options': ['Bridge'],
          '[BridgeFDB] Section Options': ['BridgeFDB'],
          '[LLDP] Section Options': ['LLDP'],
          '[CAN] Section Options': ['CAN'],
          '[IPoIB] Section Options': ['IPoIB'],
          '[QDisc] Section Options': ['QDisc'],
          '[NetworkEmulator] Section Options': ['NetworkEmulator'],
          '[TokenBucketFilter] Section Options': ['TokenBucketFilter'],
          '[PIE] Section Options': ['PIE'],
          '[FlowQueuePIE] Section Options': ['FlowQueuePIE'],
          '[StockchasticFairBlue] Section Options': ['StochasticFairBlue'],
          '[StockchasticFairnessQueueing] Section Options': ['StochasticFairnessQueueing'],
          '[BFIFO] Section Options': ['BFIFO'],
          '[PFIFO] Section Options': ['PFIFO'],
          '[PFIFOHeadDrop] Section Options': ['PFIFOHeadDrop'],
          '[PFIFOFast] Section Options': ['PFIFOFast'],
          '[CAKE] Section Options': ['CAKE'],
          '[ControlledDelay] Section Options': ['ControlledDelay'],
          '[DeficitRoundRobinScheduler] Section Options': ['DeficitRoundRobinScheduler'],
          '[DeficitRoundRobinSchedulerClass] Section Options': ['DeficitRoundRobinSchedulerClass'],
          '[EnhancedTransmissionSelection] Section Options': ['EnhancedTransmissionSelection'],
          '[GenericRandomEarlyDetection] Section Options': ['GenericRandomEarlyDetection'],
          '[FairQueueingControlledDelay] Section Options': ['FairQueueingControlledDelay'],
          '[FairQueueing] Section Options': ['FairQueueing'],
          '[TrivialLinkEqualizer] Section Options': ['TrivialLinkEqualizer'],
          '[HierarchyTokenBucket] Section Options': ['HierarchyTokenBucket'],
          '[HierarchyTokenBucketClass] Section Options': ['HierarchyTokenBucketClass'],
          '[ClassfulMultiQueueing] Section Options': ['ClassfulMultiQueueing'],
          '[BandMultiQueueing] Section Options': ['BandMultiQueueing'],
          '[HeavyHitterFilter] Section Options': ['HeavyHitterFilter'],
          '[QuickFairQueueing] Section Options': ['QuickFairQueueing'],
          '[QuickFairQueueingClass] Section Options': ['QuickFairQueueingClass'],
          '[BridgeVLAN] Section Options': ['BridgeVLAN'],

        ]
      ]
    ],
    'link':[
      'systemd.link.xml':
        ['sections': [
          '[Match] Section Options': ['Match'],
          '[Link] Section Options': ['Link'],
          '[SR-IOV] Section Options': ['SR-IOV'],
        ]
        ],
    ]
  ]

  @Internal
  Map<String /* File Type */, Map<String /* Section */, Map<String /*Keyword*/, Map<String /*Attribute*/, String /*Value*/>>>> fileTypeToSectionToKeyWordMapFromDoc = [:]

  @Internal
  final XPath xpath

  @Internal
  final DocumentBuilderFactory dbf


  @TaskAction
  void start() {
    logger.debug("Regenerating valid keys")


    fileTypeToFileAndSectionTitleToSectionName.entrySet().each {
      fileToSectionToKeyWordMapFromDoc ->   fileToSectionToKeyWordMapFromDoc.value.keySet().each {
        file ->
          logger.debug("Starting $file")
          var fileType = fileToSectionToKeyWordMapFromDoc.key
          processFile(fileType, file)
      }


    }



    logger.debug("Complete")

    def json = JsonOutput.toJson(this.fileTypeToSectionToKeyWordMapFromDoc)
    json = JsonOutput.prettyPrint(json)

    File outputData = new File(this.generatedJsonFileLocation.getAbsolutePath() + "/sectionToKeywordMapFromDoc.json")

    outputData.write(json)


    logger.debug("Output: $json")
  }

  /**
   * Processes a file from the systemd repository
   *
   *
   * @param filename
   */
  void processFile(String fileType, String filename) {
    File file = new File(this.systemdSourceCodeRoot.getAbsolutePath() + "/man/$filename")

    generateKeywordAndValueJsonMapForFile(fileType, file)

    generateDocumentationHtmlFromManPages(fileType, file)
  }

  /**
   * Opens the file that will be scanned and extracts a list of variables from it storing it in JSON
   *
   * @param File file
   */
  protected void generateKeywordAndValueJsonMapForFile(String fileType, File file) {

    String filename = file.getName()

    Document document = buildDocumentProcessingIncludes(file)

    def records = document.documentElement

    /*
      We technically should be looking for variablelist element with class 'unit-directives' however some sections in
      systemd.exec.xml don't include it, and unfortunately we can't unconditionally use variablelist because another section uses it.
      So we exclude the one other section, although this is super brittle. Yay!
     */

    NodeList result;
    if (file.getAbsolutePath().endsWith("systemd.exec.xml")) {
      result = (NodeList)xpath.evaluate(
        "/refentry/refsect1/variablelist[not(contains(@class,'environment-variables'))]/varlistentry",
        records, XPathConstants.NODESET);
    }
    else if (file.getAbsolutePath().endsWith("systemd.nspawn.xml")) {
      result = (NodeList)xpath.evaluate(
        "//variablelist[(contains(@class,'nspawn-directives'))]/varlistentry",
        records, XPathConstants.NODESET);
    } else if (file.getAbsolutePath().endsWith("systemd.netdev.xml")) {
      result = (NodeList)xpath.evaluate(
        "//variablelist[(contains(@class,'network-directives'))]/varlistentry",
        records, XPathConstants.NODESET);
    } else if (file.getAbsolutePath().endsWith("systemd.network.xml")) {
      result = (NodeList)xpath.evaluate(
        "//variablelist[(contains(@class,'network-directives'))]/varlistentry",
        records, XPathConstants.NODESET);
    } else if (file.getAbsolutePath().endsWith("systemd.link.xml")) {
      result = (NodeList)xpath.evaluate(
        "//variablelist[(contains(@class,'network-directives'))]/varlistentry",
        records, XPathConstants.NODESET);
    } else {
      result = (NodeList)xpath.evaluate(
        "//variablelist[(contains(@class,'unit-directives'))]/varlistentry",
        records, XPathConstants.NODESET);
    }

    if (result.getLength() == 0) {
      throw new IllegalStateException("Could not find variables under $filename, this file type isn't handled")
    }


    for (int i = 0; i < result.getLength(); i++) {
      Node varListEntry = result.item(i)

      NodeList variables = (NodeList)xpath.evaluate("term/varname", varListEntry, XPathConstants.NODESET)

      for (Node variable : variables) {

        String option = variable.firstChild.getTextContent()

        def (String keyName, String keyValue) = getOptionNameAndValue(option, filename)

        try {

          String titleOfSection = xpath.evaluate("ancestor::refsect1/title[text()]", varListEntry)
          List<String> sections = fileTypeToFileAndSectionTitleToSectionName[fileType][filename]['sections'][titleOfSection]

          String originalSection = xpath.evaluate("term/varname[text()]", varListEntry, XPathConstants.STRING)

          String originalKeyName = getOptionNameAndValue(originalSection, filename)[0]

          for (String section : sections) {
            logger.debug("Found options $section in $option in ${file.getAbsolutePath()}")
            fileTypeToSectionToKeyWordMapFromDoc.putIfAbsent(fileType, new TreeMap<>())
            fileTypeToSectionToKeyWordMapFromDoc.get(fileType).putIfAbsent(section, new TreeMap<>())
            def val = ["declaredInFile": filename]
            if (!keyValue.isEmpty()) val["values"] = keyValue
            if (keyName != originalKeyName) val["declaredUnderKeyword"] = originalKeyName
            fileTypeToSectionToKeyWordMapFromDoc[fileType][section][keyName] = val
          }
        }
        catch (IllegalStateException e) {
          throw e
        }
      }
    }
  }

  private static List getOptionNameAndValue(String option, String filename) {
    Matcher match = (option =~ /^([a-zA-Z0-9_-]+)=(.*)/)

    match.find()
    if (match.groupCount() != 2) {
      throw new IllegalStateException(
        "Error while processing $filename, expected that $option should conform to <Name>=<Value> format but got $option and group.size() == " +
        match.groupCount())
    }

    String name = match.group(1)
    String value = match.group(2)
    [name, value]
  }

  public GenerateDataFromManPages() {
    xpath = XPathFactory.newInstance().newXPath()

    dbf = DocumentBuilderFactory.newInstance()
    dbf.setXIncludeAware(true)
    //dbf.setNamespaceAware(true)
    dbf.setValidating(false)
    dbf.setExpandEntityReferences(false)

    dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false)
    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
  }

  /**
   * Generates individual HTML files for use as inline documentation
   *
   * We proceed in two steps
   *
   * @param File sourceFile - the source file to extract
   * @return
   */
  protected generateDocumentationHtmlFromManPages(String fileType, File sourceFile) {


    Document document = buildDocumentProcessingIncludes(sourceFile)
    Transformer transformer = getXsltTransformer()

    String xsltOutput = transformDocument(document, transformer)

    segmentParametersIntoFiles(fileType, sourceFile.getName(), xsltOutput)
  }

  protected Document buildDocumentProcessingIncludes(File sourceFile) {
    DocumentBuilder builder = dbf.newDocumentBuilder()
    String xmlContent = sourceFile.text


    // I spent an hour with ChatGPT trying to get
    // Xincludes to work properly (without spending 6 hours to understand them).
    // Couldn't get it to work, due to Java not liking things like includes with no href, for local references.
    // 💣 Step 1: Replace XInclude elements using regex
    xmlContent = processXIncludesWithRegex(xmlContent, sourceFile.parentFile)


    File outputDir = project.layout.buildDirectory.dir("tmp/rendered-xincludes").get().asFile
    if (!outputDir.exists()) {
      outputDir.mkdirs()
    }

    File outputFile = new File(outputDir, sourceFile.getName())
    outputFile.text = xmlContent  // Save to file

    Document document = builder.parse(outputFile)
    document
  }

  protected String processXIncludesWithRegex(String xmlContent, File baseDir) {
    // 🔥 Regex to match <xi:include href="some.xml" xpointer="some-id"/> (xpointer is optional)
    def includePattern = /<xi:include\s+href="([^"]+)"(?:\s+xpointer="([^"]+)")?\s*\/>/

    return xmlContent.replaceAll(includePattern) { match, href, xpointer ->
      File includedFile = new File(baseDir, href)

      if (!includedFile.exists()) {
        println "⚠️ WARNING: Included file '${includedFile.absolutePath}' not found!"
        return "<!-- Failed to include: $href -->"
      }


      // ✅ Load XML properly instead of using regex
      String xptr = xpointer
      String includedContent = GenerateDataFromManPages.extractElementById(includedFile, xptr)

      return includedContent ?: "<!-- Failed to find xpointer '$xpointer' in $href -->"

    }
  }

  // 🔥 Static cache for storing extracted XML elements
  private static final Map<String, Map<String, String>> fileCache = new ConcurrentHashMap<>()

  static String extractElementById(File xmlFile, String elementId) {
    // ✅ Check if the entire file has already been cached
    String filePath = xmlFile.getAbsolutePath()
    if (!fileCache.containsKey(filePath)) {
      // 🚀 Populate the cache for this file
      cacheAllElements(xmlFile)
    }

    // ✅ Retrieve element from cache
    Map<String, String> cachedElements = fileCache.get(filePath)

    if (elementId == null) {
      elementId = DOCUMENT_CACHE_KEY
    }

    if (cachedElements.containsKey(elementId)) {
      return cachedElements.get(elementId)
    } else {
      println "⚠️ WARNING: Element with id='$elementId' not found in ${xmlFile.name}"
      return null
    }
  }

  private static final String DOCUMENT_CACHE_KEY = "[[[ROOT_ELEMENT]]]"

  private static void cacheAllElements(File xmlFile) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
      factory.setNamespaceAware(true) // Needed for XML ID lookup
      factory.setValidating(false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      DocumentBuilder builder = factory.newDocumentBuilder()

      Document document = builder.parse(new InputSource(xmlFile.newReader()))
      document.getDocumentElement().normalize()

      // ✅ Create a new cache for this file
      Map<String, String> elementCache = new ConcurrentHashMap<>()

      String rootDoc = nodeToString(document.documentElement)
      elementCache.put(DOCUMENT_CACHE_KEY, rootDoc)
      // 🚀 Find all elements with an `id` attribute and store them in cache
      NodeList elements = document.getElementsByTagName("*")
      for (int i = 0; i < elements.length; i++) {
        Element element = elements.item(i)
        if (element.hasAttribute("id")) {
          String elementId = element.getAttribute("id")
          String extractedXml = nodeToString(element)

          String wrappedXml = "<!--xi:include='${xmlFile.name}' xpointer='${elementId}'-->" +
                              extractedXml +
                              "<!-- /xi:include='${xmlFile.name}' xpointer='${elementId}' -->"

          elementCache.put(elementId, wrappedXml)
        }
      }

      // ✅ Store parsed elements in the file cache
      fileCache.put(xmlFile.getAbsolutePath(), elementCache)

      println "✅ Cached ${elementCache.size()} elements from ${xmlFile.name}"

    } catch (Exception e) {
      println "❌ ERROR: Failed to parse ${xmlFile.name}: ${e.message}"
    }
  }

  private static String nodeToString(Node node) {
    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance()
      Transformer transformer = transformerFactory.newTransformer()
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
      StringWriter writer = new StringWriter()
      transformer.transform(new DOMSource(node), new StreamResult(writer))
      return writer.toString()
    } catch (Exception e) {
      println "❌ ERROR: Failed to convert node to string: ${e.message}"
      return ""
    }
  }

  /**
   * Transforms the supplied document with the supplied transformer
   * @param document - XML Document to transform
   * @param transformer - Transformer (i.e., a representation of the XSLT).
   *
   * @return XML output as string
   */
  private static String transformDocument(Document document, Transformer transformer) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream()
    StreamResult result = new StreamResult(baos)
    DOMSource source = new DOMSource(document)

    transformer.transform(source, result)
    String xsltOutput = new String(baos.toByteArray(), "UTF-8")
    xsltOutput
  }

  /**
   * Constructs the XSLT transformer
   *
   * @param sourceFile
   *
   * @return a Transformer instance configured with the XSLT
   */
  private Transformer getXsltTransformer() {

    StreamSource styleSource = new StreamSource(this.getClass().getClassLoader().getResourceAsStream("transformManPages.xslt"))

    TransformerFactory teFactory = TransformerFactory.newInstance()

    Transformer transformer = teFactory.newTransformer(styleSource)
    //transformer.setParameter("systemd.version", "295")

    return transformer
  }

  /**
   * Converts a post XSLT transformed document into individual files.
   *
   * Expected structure of the XML is:
   *
   * <parameterlist>
   *   <parameter>
   *     <name>..</name>
   *     <section>..</section>
   *     <description>
   *       <paragraphList>..</paragraphList>
   *       ...
   *     </description>
   *   </parameter>
   *   ...
   * <parameterlist>
   *
   *
   * @param sourceFileName - the name of the source file we pulled the data from
   * @param parameterInfoXMLAsString - A transformed XML document representing the documentation for systemd
   */
  protected void segmentParametersIntoFiles(String fileType, String sourceFileName, String parameterInfoXMLAsString) {
    def builder = dbf.newDocumentBuilder()

    ByteArrayInputStream bis = new ByteArrayInputStream(parameterInfoXMLAsString.getBytes("UTF-8"))
    def records = builder.parse(bis).documentElement

    NodeList result = (NodeList)xpath.
      evaluate("/parameterlist/parameter", records, XPathConstants.NODESET)


    for (int i = 0; i < result.getLength(); i++) {
      Node parameterNode = result.item(i)
      String variableName = ((Node)xpath.evaluate("name", parameterNode, XPathConstants.NODE)).getTextContent()

      String sectionTitle = ((Node)xpath.evaluate("section", parameterNode, XPathConstants.NODE)).getTextContent()

      NodeList paragraphList = (NodeList)xpath.evaluate("description/paragraph", parameterNode, XPathConstants.NODESET)

      Matcher match = (variableName =~ /([^=]+)=(.*)/)

      match.find()
      if (match.groupCount() != 2) {
        throw new IllegalStateException(
          "Error while processing $sourceFileName, expected that $variableName should conform to <Name>=<Value> format but got $variableName and group.size() == " +
          match.groupCount())
      }

      String name = match.group(1)

      List<String> foo = fileTypeToFileAndSectionTitleToSectionName[fileType][sourceFileName]['sections'][sectionTitle]

      for (String sectionName : foo) {
        File outputFile = new File(
          this.generatedJsonFileLocation.getAbsolutePath() + "/documents/completion/" + fileType + "/" + sectionName + "/" + name + ".html")
        outputFile.getParentFile().mkdirs()

        Writer write = new BufferedWriter(new FileWriter(outputFile))

        Node paragraphContent
        for (int j = 0; j < paragraphList.getLength(); j++) {
          paragraphContent = paragraphList.item(j)

          write.write("<p>")
          write.write(innerXml(paragraphContent))
          write.write("</p>\n")
        }

        write.flush()
        write.close()
      }
    }
  }

  /**
   * Hacky method which takes a node and converts it into an XML string again
   *
   * We are doing this essentially because we want to treat the inner nodes of <paragraph> as HTML and just include it's content.
   *
   * A better developer would likely have changed the XSLT to generate <paragraph><![CDATA[... HTML ...]]></paragraph>, but this
   * developer couldn't figure that out, so here you, some hack I pulled off Stack Overflow.
   *
   * https://stackoverflow.com/questions/2784183/what-does-cdata-in-xml-mean
   *
   *
   * @param node - the node to convert into a string
   * @return XML representation of the node and all it's descendents.
   */
  private static String innerXml(Node node) {
    DOMImplementationLS lsImpl = (DOMImplementationLS)node.getOwnerDocument().getImplementation().getFeature("LS", "3.0")
    LSSerializer lsSerializer = lsImpl.createLSSerializer()
    lsSerializer.getDomConfig().setParameter("xml-declaration", false)
    NodeList childNodes = node.getChildNodes()
    StringBuilder sb = new StringBuilder()

    for (int i = 0; i < childNodes.getLength(); i++) {
      sb.append(lsSerializer.writeToString(childNodes.item(i)))
    }
    return sb.toString()
  }
}
