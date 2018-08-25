import groovy.json.JsonOutput
import org.apache.commons.io.output.TeeOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
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
  def fileAndSectionTitleToSectionName = [
    'systemd.unit.xml' :
      [ 'sections':
        [ '[Unit] Section Options' : ['Unit'],
          '[Install] Section Options' : ['Install']
        ]
      ],
    'systemd.service.xml' :
     [ 'sections':
        ['Options': ['Service']]
     ],
    'systemd.timer.xml' :
      [ 'sections':
          ['Options': ['Timer']]
      ],
    'systemd.automount.xml' :
      [ 'sections':
          ['Options': ['Automount']]
      ],
    'systemd.mount.xml' :
      [ 'sections':
          ['Options': ['Mount']]
      ],
    'systemd.path.xml' :
      [ 'sections':
          ['Options': ['Path']]
      ],
    'systemd.socket.xml' :
      [ 'sections':
          ['Options': ['Socket']]
      ],
    'systemd.swap.xml' :
      [ 'sections':
          ['Options': ['Swap']]
      ],
    'systemd.resource-control.xml':
      ['sections':
         ['Options': ['Slice', 'Service', 'Socket', 'Mount', 'Swap']]
      ],
    'systemd.kill.xml':
      [ 'sections':
          ['Options': ['Service', "Socket", "Mount", "Swap"]]
      ],
    'systemd.exec.xml':
      ['sections':
         [
           'Paths'                            : ['Service', 'Socket', 'Mount', 'Swap'],
           'Credentials'                      : ['Service', 'Socket', 'Mount', 'Swap'],
           'Capabilities'                     : ['Service', 'Socket', 'Mount', 'Swap'],
           'Security'                         : ['Service', 'Socket', 'Mount', 'Swap'],
           'Mandatory Access Control'         : ['Service', 'Socket', 'Mount', 'Swap'],
           'Process Properties'               : ['Service', 'Socket', 'Mount', 'Swap'],
           'Scheduling'                       : ['Service', 'Socket', 'Mount', 'Swap'],
           'Sandboxing'                       : ['Service', 'Socket', 'Mount', 'Swap'],
           'System Call Filtering'            : ['Service', 'Socket', 'Mount', 'Swap'],
           'Environment'                      : ['Service', 'Socket', 'Mount', 'Swap'],
           'Logging and Standard Input/Output': ['Service', 'Socket', 'Mount', 'Swap'],
           'System V Compatibility'           : ['Service', 'Socket', 'Mount', 'Swap']
         ]
      ]
  ]

  Map<String /* Section */, Map<String /*Keyword*/, Map<String /*Attribute*/, String /*Value*/>>> sectionToKeyWordMap = [:]

  final XPath xpath

  final DocumentBuilderFactory dbf

  public GenerateDataFromManPages()
  {
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
    logger.error("Regenerating valid keys")


    fileAndSectionTitleToSectionName.keySet().each { file ->
      logger.error("Starting $file")
      processForFile(file)

    }

    logger.error("Complete")

    def json = JsonOutput.toJson(this.sectionToKeyWordMap)
    json = JsonOutput.prettyPrint(json)

    File outputData = new File(this.generatedJsonFileLocation.getAbsolutePath() + "/sectionToKeywordMap.json")

    outputData.write(json)


    logger.error("Output: $json")

  }

  void processForFile(String filename)
  {
    File file = new File(this.systemdSourceCodeRoot.getAbsolutePath() + "/man/$filename")


    transformXMLWithXSLT(file)


    def builder     = dbf.newDocumentBuilder()

    def records     = builder.parse(file).documentElement

    //


    /*
      We should only be using unit-directives as a class selector on the variable list,
      however the man pages inconsistently use it in systemd.exec.xml.

      "/refentry/refsect1/variablelist[@class='unit-directives']/varlistentry"


     */
    NodeList result = (NodeList)xpath.
      evaluate("/refentry/refsect1/variablelist/varlistentry", records, XPathConstants.NODESET)

    for(int i = 0; i < result.getLength(); i++)
    {
      Node varListEntry = result.item(i)

      Node variableListClass = varListEntry.getParentNode().getAttributes().getNamedItem("class")
      if((variableListClass != null) && variableListClass.getTextContent().equals("environment-variables"))
      {
        continue
      }
      NodeList variables = (NodeList)xpath.evaluate("term/varname", varListEntry, XPathConstants.NODESET)

      for (Node variable : variables) {


        String option = variable.firstChild.getTextContent()

        def (String keyName, String keyValue) = getOptionNameAndValue(option, filename)

        try {


          String titleOfSection = xpath.evaluate("../../title[text()]", varListEntry)
          List<String> sections = fileAndSectionTitleToSectionName[filename]['sections'][titleOfSection]

          String originalSection = xpath.evaluate("term/varname[text()]", varListEntry, XPathConstants.STRING)

          String originalKeyName = getOptionNameAndValue(originalSection, filename)[0]

          for (String section : sections) {
            logger.error("Found options $section in $option")
            sectionToKeyWordMap.putIfAbsent(section, new TreeMap<>())
            sectionToKeyWordMap[section][keyName] = ["values": keyValue, "declaredUnderKeyword" : originalKeyName, "declaredInFile" : filename]
          }
        } catch(IllegalStateException e)
        {
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

  def transformXMLWithXSLT(File sourceFile)
  {

    DocumentBuilder builder = dbf.newDocumentBuilder()

    Document document = builder.parse(sourceFile)

    StreamSource styleSource = new StreamSource(this.getClass().getClassLoader().getResourceAsStream("transformManPages.xslt"))

    TransformerFactory teFactory = TransformerFactory.newInstance()

    Transformer transformer = teFactory.newTransformer(styleSource)
    transformer.setParameter("systemd.version", "295")

    new File(this.generatedJsonFileLocation.getAbsolutePath() + "/documents/").mkdirs()

    File output = new File(this.generatedJsonFileLocation.getAbsolutePath() + "/documents/" + sourceFile.getName())

    ByteArrayOutputStream baos = new ByteArrayOutputStream()

    OutputStream out = new TeeOutputStream(new FileOutputStream(output), baos)

    StreamResult result = new StreamResult(out)

    DOMSource source = new DOMSource(document)

    transformer.transform(source, result)

    String xmlInput = new String(baos.toByteArray(), "UTF-8")

    segmentParametersIntoFiles(sourceFile.getName(), xmlInput)

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
   * @param sourceFileName
   * @param parameterInfoXMLAsString
   */
  def segmentParametersIntoFiles(String sourceFileName, String parameterInfoXMLAsString) {
    def builder = dbf.newDocumentBuilder()

    ByteArrayInputStream bis = new ByteArrayInputStream(parameterInfoXMLAsString.getBytes("UTF-8"))
    def records = builder.parse(bis).documentElement

    NodeList result = (NodeList)xpath.
      evaluate("/parameterlist/parameter", records, XPathConstants.NODESET)


    for (int i = 0; i < result.getLength(); i++) {
      Node parameterNode = result.item(i)
      String variableName = ((Node) xpath.evaluate("name", parameterNode, XPathConstants.NODE)).getTextContent()

      String sectionTitle = ((Node) xpath.evaluate("section", parameterNode, XPathConstants.NODE)).getTextContent()

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

      for(String sectionName : foo)
      {
        File outputFile = new File(this.generatedJsonFileLocation.getAbsolutePath() + "/documents/completion/" + sectionName + "/" + name + ".html")
        outputFile.getParentFile().mkdirs()

        Writer write = new BufferedWriter(new FileWriter(outputFile))

        Node paragraphContent
        for(int j = 0; j < paragraphList.getLength(); j++)
        {
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
