import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Internal
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.ls.DOMImplementationLS
import org.w3c.dom.ls.LSSerializer

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
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
  def fileAndSectionTitleToSectionName = [
    'systemd.unit.xml'            :
      ['sections':
         ['[Unit] Section Options'   : ['Unit'],
          '[Install] Section Options': ['Install'],
           'Conditions and Asserts' : ['Unit']
         ]
      ],
    'systemd.service.xml'         :
      ['sections':
         ['Options': ['Service']]
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
      ]
  ]

  @Internal
  Map<String /* Section */, Map<String /*Keyword*/, Map<String /*Attribute*/, String /*Value*/>>> sectionToKeyWordMapFromDoc = [:]

  @Internal
  final XPath xpath

  @Internal
  final DocumentBuilderFactory dbf

  public GenerateDataFromManPages() {
    xpath = XPathFactory.newInstance().newXPath()

    dbf = DocumentBuilderFactory.newInstance()
    dbf.setValidating(false)
    dbf.setExpandEntityReferences(false)
    dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false)
    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
  }


  @TaskAction
  void start() {
    logger.debug("Regenerating valid keys")


    fileAndSectionTitleToSectionName.keySet().each { file ->
      logger.debug("Starting $file")
      processFile(file)
    }

    logger.debug("Complete")

    def json = JsonOutput.toJson(this.sectionToKeyWordMapFromDoc)
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
  void processFile(String filename) {
    File file = new File(this.systemdSourceCodeRoot.getAbsolutePath() + "/man/$filename")

    generateKeywordAndValueJsonMapForFile(file)

    generateDocumentationHtmlFromManPages(file)
  }

  /**
   * Opens the file that will be scanned and extracts a list of variables from it storing it in JSON
   *
   * @param File file
   */
  private void generateKeywordAndValueJsonMapForFile(File file) {

    String filename = file.getName()

    def builder = dbf.newDocumentBuilder()

    def records = builder.parse(file).documentElement

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
    else {
      result = (NodeList)xpath.evaluate(
        "//variablelist[(contains(@class,'unit-directives'))]/varlistentry",
        records, XPathConstants.NODESET);
    }


    for (int i = 0; i < result.getLength(); i++) {
      Node varListEntry = result.item(i)

      NodeList variables = (NodeList)xpath.evaluate("term/varname", varListEntry, XPathConstants.NODESET)

      for (Node variable : variables) {

        String option = variable.firstChild.getTextContent()

        def (String keyName, String keyValue) = getOptionNameAndValue(option, filename)

        try {

          String titleOfSection = xpath.evaluate("ancestor::refsect1/title[text()]", varListEntry)
          List<String> sections = fileAndSectionTitleToSectionName[filename]['sections'][titleOfSection]

          String originalSection = xpath.evaluate("term/varname[text()]", varListEntry, XPathConstants.STRING)

          String originalKeyName = getOptionNameAndValue(originalSection, filename)[0]

          for (String section : sections) {
            logger.debug("Found options $section in $option in ${file.getAbsolutePath()}")
            sectionToKeyWordMapFromDoc.putIfAbsent(section, new TreeMap<>())
            def val = ["declaredInFile": filename]
            if (!keyValue.isEmpty()) val["values"] = keyValue
            if (keyName != originalKeyName) val["declaredUnderKeyword"] = originalKeyName
            sectionToKeyWordMapFromDoc[section][keyName] = val
          }
        }
        catch (IllegalStateException e) {
          throw e
        }
      }
    }
  }

  private static List getOptionNameAndValue(String option, String filename) {
    Matcher match = (option =~ /(\w+)=(.*)/)

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

  /**
   * Generates individual HTML files for use as inline documentation
   *
   * We proceed in two steps
   *
   * @param File sourceFile - the source file to extract
   * @return
   */
  private generateDocumentationHtmlFromManPages(File sourceFile) {
    DocumentBuilder builder = dbf.newDocumentBuilder()
    Document document = builder.parse(sourceFile)
    Transformer transformer = getXsltTransformer()

    String xsltOutput = transformDocument(document, transformer)

    segmentParametersIntoFiles(sourceFile.getName(), xsltOutput)
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
  private void segmentParametersIntoFiles(String sourceFileName, String parameterInfoXMLAsString) {
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

      Matcher match = (variableName =~ /(\w+)=(.*)/)

      match.find()
      if (match.groupCount() != 2) {
        throw new IllegalStateException(
          "Error while processing $sourceFileName, expected that $variableName should conform to <Name>=<Value> format but got $variableName and group.size() == " +
          match.groupCount())
      }

      String name = match.group(1)

      List<String> foo = fileAndSectionTitleToSectionName[sourceFileName]['sections'][sectionTitle]

      for (String sectionName : foo) {
        File outputFile = new File(
          this.generatedJsonFileLocation.getAbsolutePath() + "/documents/completion/" + sectionName + "/" + name + ".html")
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
