import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Node
import org.w3c.dom.NodeList

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import java.util.regex.Matcher

/**
 * This task scans the systemd source code man pages to extract the set of available options as well as (eventually) documentation.
 *
 * The logic in this class relies on some structuring assumptions on the source XML, namely
 * that all options are under a node:
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
class ExtractValidKeysFromManPage extends DefaultTask {

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
         ['Options': ['Slice', 'Scope', 'Service', 'Socket', 'Mount', 'Swap']]
      ],
    'systemd.exec.xml':
      ['sections':
         [
           'Paths'                            : ['Service', 'Socket', 'Mount', 'Swap'],
           'Credentials'                      : ['Service', 'Socket', 'Mount', 'Swap'],
           'Capabilities'                     : ['Service', 'Socket', 'Mount', 'Swap'],
           'Security'                         : ['Service', 'Socket', 'Mount', 'Swap'],
           'Manadatory Access Control'        : ['Service', 'Socket', 'Mount', 'Swap'],
           'Process Properties'               : ['Service', 'Socket', 'Mount', 'Swap'],
           'Scheduling'                       : ['Service', 'Socket', 'Mount', 'Swap'],
           'System Call Filtering'            : ['Service', 'Socket', 'Mount', 'Swap'],
           'Environment'                      : ['Service', 'Socket', 'Mount', 'Swap'],
           'Loggind and Standard Input/Output': ['Service', 'Socket', 'Mount', 'Swap'],
           'System V Compatibility'           : ['Service', 'Socket', 'Mount', 'Swap']
         ]
      ]
  ]

  Map<String /* Section */, Map<String /*Keyword*/, Map<String /*Attribute*/, String /*Value*/>>> sectionToKeyWordMap = [:]

  public ExtractValidKeysFromManPage()
  {

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

    def xpath = XPathFactory.newInstance().newXPath()

    def dbf = DocumentBuilderFactory.newInstance()
    dbf.setValidating(false)
    dbf.setExpandEntityReferences(false)
    dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false)
    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false)

    def builder     = dbf.newDocumentBuilder()

    def records     = builder.parse(file).documentElement

    NodeList result = (NodeList)xpath.
      evaluate("/refentry/refsect1/variablelist[@class='unit-directives']/varlistentry", records, XPathConstants.NODESET)

    for(int i = 0; i < result.getLength(); i++)
    {
      Node node = result.item(i)

      /*
       * TODO clean up this garbage, all the warnings
       */
      NodeList variables = (NodeList)xpath.evaluate("term/varname", node, XPathConstants.NODESET)

      for (Node variable : variables) {


        String option = variable.firstChild.getTextContent()

        Matcher match = (option =~ /(\w+)=(.*)/)

        match.find()
        if (match.groupCount() != 2) {
          throw new IllegalStateException(
            "Error while processing $filename, expected that $option should conform to <Name>=<Value> format but got $option and group.size() == " +
            match.groupCount())
        }

        String name = match.group(1)
        String value = match.group(2)

        String titleOfSection = xpath.evaluate("../../title[text()]", node)
        List<String> sections = fileAndSectionTitleToSectionName[filename]['sections'][titleOfSection]

        for (String section : sections) {
          logger.error("Found options $section in $option")
          sectionToKeyWordMap.putIfAbsent(section, new TreeMap<>())
          sectionToKeyWordMap[section][name] = ["values": value]
        }
      }
    }
  }
}
