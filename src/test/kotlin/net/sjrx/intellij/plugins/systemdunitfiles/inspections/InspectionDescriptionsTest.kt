package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import org.junit.Assert.assertEquals
import org.junit.Test
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Every inspection in plugin.xml needs `inspectionDescriptions/<shortName>.html`. Without it the
 * Settings | Editor | Inspections page shows "Can't find inspection description".
 */
class InspectionDescriptionsTest {

  @Test
  fun everyRegisteredInspectionHasADescription() {
    val loader = javaClass.classLoader
    val pluginXml = loader.getResourceAsStream("META-INF/plugin.xml")!!.use {
      DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it)
    }

    val shortNames = listOf("localInspection", "globalInspection").flatMap { tag ->
      val nodes = pluginXml.getElementsByTagName(tag)
      (0 until nodes.length).map { nodes.item(it).attributes.getNamedItem("shortName").nodeValue }
    }

    val missing = shortNames.filter { loader.getResource("inspectionDescriptions/$it.html") == null }
    assertEquals("Inspections without a description file", emptyList<String>(), missing)
  }
}
