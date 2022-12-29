package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonFactoryBuilder
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.ObjectUtils
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.*
import org.apache.commons.io.IOUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern

class SemanticDataRepository private constructor() {
  private val validatorMap: Map<Validator, OptionValueInformation>
  private val sectionToKeyAndValidatorMap: MutableMap<String, MutableMap<String, Validator>> = HashMap()
  private val sectionNameToKeyValuesFromDoc: MutableMap<String, Map<String, KeywordData>>
  private val undocumentedOptionInfo: Map<String, Map<String, KeywordData>>

  class KeywordData {
    var declaredUnderKeyword: String? = null
    var declaredInFile: String? = null
    var reason: String? = null
    var documentationLink: String? = null
    var replacedWithKey: String? = null
    var replacedWithSection: String? = null
    var description: String? = null
    var values: String? = null
    var unsupported: String? = null
  }

  init {
    val cache: Map<String, String> = HashMap()
    val cache2: Map<Validator, Validator> = HashMap()

    sectionNameToKeyValuesFromDoc = loadSemanticDataFromJsonFile(SEMANTIC_DATA_ROOT + "sectionToKeywordMapFromDoc.json", cache)
    undocumentedOptionInfo = loadSemanticDataFromJsonFile(SEMANTIC_DATA_ROOT + "undocumentedSectionToKeywordMap.json", cache)
    validatorMap = HashMap()
    val resource = this.javaClass.classLoader.getResourceAsStream(SEMANTIC_DATA_ROOT + "load-fragment-gperf.gperf")
    if (resource != null) {
      try {
        BufferedReader(InputStreamReader(resource)).use { fr ->
          var line: String?
          while (fr.readLine().also { line = it } != null) {
            val m = LINE_MATCHER.matcher(line)
            if (m.find()) {
              val section = m.group("Section")
              val key = m.group("Key")
              val validator = m.group("Validator")
              val mysteryValue = m.group("MysteryColumn")
              when (mysteryValue) {
                LEGACY_PARAMETERS_KEY, EXPERIMENTAL_PARAMETERS_KEY -> {}


                else -> {
                  val validator = Validator(validator, mysteryValue)
                  sectionToKeyAndValidatorMap.computeIfAbsent(intern(cache, section)) { k: String? -> HashMap() }[intern(cache, key)] = intern(cache2, validator)
                }
              }
            }
          }

          validatorMap.putAll(BooleanOptionValue.validators)
          validatorMap.putAll(DocumentationOptionValue.validators)
          validatorMap.putAll(KillModeOptionValue.validators)
          validatorMap.putAll(ModeStringOptionValue.validators)
          validatorMap.putAll(RestartOptionValue.validators)
          validatorMap.putAll(ServiceTypeOptionValue.validators)
          validatorMap.putAll(ExecOptionValue.validators)
          validatorMap.putAll(UnitDependencyOptionValue.validators)
          validatorMap.putAll(NullOptionValue.validators)
          validatorMap.putAll(MemoryLimitOptionValue.validators)
          validatorMap.putAll(SignalOptionValue.validators)
          validatorMap.putAll(NamespacePathOptionValue.validators)

         // Scopes are not supported since they aren't standard unit files.

          sectionNameToKeyValuesFromDoc.remove(SCOPE_KEYWORD)
          sectionToKeyAndValidatorMap.remove(SCOPE_KEYWORD)
        }
      } catch (e: IOException) {
        LOG.error("Unable to initialize data for systemd inspections plugin", e)
      }
    } else {
      LOG.error("Unable to initialize data for systemd inspections plugin, resource not found")
    }
  }

  private fun loadSemanticDataFromJsonFile(filename: String, cache: Map<String, String>): MutableMap<String, Map<String, KeywordData>> {
    val sectionToKeywordMapFromDocJsonFile = this.javaClass.classLoader.getResource(filename)
    val factory = JsonFactoryBuilder().disable(JsonFactory.Feature.INTERN_FIELD_NAMES).build()
    if (!ApplicationManager.getApplication().isUnitTestMode) {
      factory.disable(JsonParser.Feature.IGNORE_UNDEFINED)
    }

    val mapper = ObjectMapper(factory)
    mapper.registerModule(KotlinModule.Builder().build())

    try {
      val read : Map<String, Map<String, KeywordData>> = mapper.readValue(sectionToKeywordMapFromDocJsonFile)

    // intern strings in maps
    val output: MutableMap<String, Map<String, KeywordData>> = HashMap()

    read.forEach { (section: String, m1: Map<String, KeywordData>) ->
      val converted = HashMap<String, KeywordData>()
      m1.forEach { (keyword: String, m2: KeywordData) -> converted[intern(cache, keyword)] = m2 }
      output[intern(cache, section)] = converted
    }
    return output

    } catch (e: IOException) {
      LOG.error("Unable to initialize data for systemd inspections plugin", e)
      return HashMap()


    }
  }

  val sectionNamesFromDocumentation: Set<String>
    /**
     * Returns the allowed section names.
     *
     * @return a set of allow section names
     */
    get() = Collections.unmodifiableSet(sectionNameToKeyValuesFromDoc.keys)

  /**
   * Returns the allowed keywords by a given section name.
   *
   * @param section the section name (e.g., Unit, Install, Service)
   * @return set of allowed names.
   */
  fun getDocumentedKeywordsInSection(section: String): Set<String> {
    val keys: MutableSet<String> = HashSet(getKeyValuePairsForSectionFromDocumentation(section).keys)
    keys.addAll(getKeyValuePairsForSectionFromUndocumentedInformation(section).keys)
    return Collections.unmodifiableSet(keys)
  }

  /**
   * Returns the documentation url for a section and keyname.
   *
   * @param section - section name
   * @param keyName - key name
   * @return String or null
   */
  fun getKeywordDocumentationUrl(section: String, keyName: String): String? {
    var data = getKeyValuePairsForSectionFromDocumentation(section)[keyName]
    if (data != null && data.documentationLink != null) {
      return data.documentationLink
    }
    data = getKeyValuePairsForSectionFromUndocumentedInformation(section)[keyName]
    return if (data != null && data.documentationLink != null) {
      data.documentationLink
    } else null
  }

  /**
   * Returns the location (by keyword) for a specific keyword.
   *
   *
   * Some keys are documented together in systemd (e.g., After= and Before=), we can only
   * link to one of them, so this method will tell us what to link to.
   *
   * @param section - section name
   * @param keyName - key name
   * @return String or null
   */
  fun getKeywordLocationInDocumentation(section: String, keyName: String): String? {
    val data = getKeyValuePairsForSectionFromDocumentation(section)[keyName] ?: return null
    return ObjectUtils.notNull(data.declaredUnderKeyword, keyName)
  }

  /**
   * Returns the location in filename for a specific keyword.
   *
   *
   * Some keys are shared between many different types of units (e.g., WorkingDirectory= is in systemd.exec but shows up in [Service],
   * [Socket], etc...
   *
   * @param section - section name
   * @param keyName - key name
   * @return String or null
   */
  fun getKeywordFileLocationInDocumentation(section: String, keyName: String): String? {
    val data = getKeyValuePairsForSectionFromDocumentation(section)[keyName]
    return data?.declaredInFile
  }

  fun getKeyValuePairsForSectionFromDocumentation(section: String): Map<String, KeywordData> {
    return sectionNameToKeyValuesFromDoc[section] ?: emptyMap()
  }

  fun getKeyValuePairsForSectionFromUndocumentedInformation(section: String): Map<String, KeywordData> {
    return undocumentedOptionInfo[section] ?: emptyMap()
  }

  /**
   * Returns a URL to the section name man page.
   *
   * @param sectionName - the name of the section name
   * @return best URL for the section name or null if the section is unknown
   */
  fun getUrlForSectionName(sectionName: String?): String? {
    return when (sectionName) {
      "Mount" -> "https://www.freedesktop.org/software/systemd/man/systemd.mount.html"
      "Automount" -> "https://www.freedesktop.org/software/systemd/man/systemd.automount.html"
      "Install" -> "https://www.freedesktop.org/software/systemd/man/systemd.unit.html#%5BInstall%5D%20Section%20Options"
      "Path" -> "https://www.freedesktop.org/software/systemd/man/systemd.path.html"
      "Service" -> "https://www.freedesktop.org/software/systemd/man/systemd.service.html"
      "Slice" -> "https://www.freedesktop.org/software/systemd/man/systemd.slice.html"
      "Socket" -> "https://www.freedesktop.org/software/systemd/man/systemd.socket.html"
      "Swap" -> "https://www.freedesktop.org/software/systemd/man/systemd.swap.html"
      "Timer" -> "https://www.freedesktop.org/software/systemd/man/systemd.timer.html"
      "Unit" -> "https://www.freedesktop.org/software/systemd/man/systemd.unit.html#%5BUnit%5D%20Section%20Options"
      else -> null
    }
  }

  /**
   * Returns a document blurb for a Section.
   *
   * @param sectionName the name of the section.
   * @return string
   */
  fun getDocumentationContentForSection(sectionName: String?): String? {
    return when (sectionName) {
      "Mount" -> """ Mount files must include a [Mount] section, which carries information
       about the file system mount points it supervises. A number of options
       that may be used in this section are shared with other unit types.
       These options are documented in <a href="http://man7.org/linux/man-pages/man5/systemd.exec.5.html">systemd.exec(5)</a> and <a href="http://man7.org/linux/man-pages/man5/systemd.kill.5.html">systemd.kill(5)</a>."""

      "Automount" -> """Automount files must include an [Automount] section, which carries
       information about the file system automount points it supervises."""

      "Install" -> """Unit files may include an "[Install]" section, which carries
       installation information for the unit. This section is not
       interpreted by <a href="http://man7.org/linux/man-pages/man1/systemd.1.html">systemd(1)</a> during runtime; it is       used by the <b>enable</b> and <b>disable </b>commands of the       <a href="http://man7.org/linux/man-pages/man1/systemctl.1.html">systemctl(1)</a> tool during installation of
       a unit. """

      "Path" -> """Path files must include a [Path] section, which carries information
       about the path(s) it monitors"""

      "Service" -> """Service files must include a "[Service]" section, which carries
information about the service and the process it supervises. A number
of options that may be used in this section are shared with other
unit types. These options are documented in <a href="http://man7.org/linux/man-pages/man5/systemd.exec.5.html">systemd.exec(5)</a>,
<a href="http://man7.org/linux/man-pages/man5/systemd.kill.5.html">systemd.kill(5)</a> and <a href="http://man7.org/linux/man-pages/man5/systemd.resource-control.5.html">systemd.resource-control(5)</a>."""

      "Slice" -> """The slice specific configuration
       options are configured in the [Slice] section. Currently, only
       generic resource control settings as described in
       <a href="http://man7.org/linux/man-pages/man5/systemd.resource-control.5.html">systemd.resource-control(5)</a> are allowed.
"""

      "Socket" -> """Socket files must include a [Socket] section, which carries
       information about the socket or FIFO it supervises. A number of
       options that may be used in this section are shared with other unit
       types. These options are documented in <a href="http://man7.org/linux/man-pages/man5/systemd.exec.5.html">       systemd.exec(5)</a> and <a href="http://man7.org/linux/man-pages/man5/systemd.kill.5.html">systemd.kill(5)</a>."""

      "Swap" -> """Swap files must include a [Swap] section, which carries information
 about the swap device it supervises. A number of options that may be
 used in this section are shared with other unit types. These options
 are documented in <a href="http://man7.org/linux/man-pages/man5/systemd.exec.5.html">systemd.exec(5)</a> and <a href="http://man7.org/linux/man-pages/man5/systemd.kill.5.html">systemd.kill(5)</a>"""

      "Timer" -> """ Timer files must include a [Timer] section, which carries information
 about the timer it defines."""

      "Unit" -> """The unit file may include a [Unit] section, which carries generic
 information about the unit that is not dependent on the type of unit."""

      else -> null
    }
  }

  /**
   * Return the documentation for this key.
   *
   * @param sectionName the section name to lookup (e.g., Unit, Install, Service)
   * @param keyName     the key name to look up
   * @return either the first paragraph from the HTML description or null if no description was found
   */
  fun getDocumentationContentForKeyInSection(sectionName: String, keyName: String): String? {
    val htmlDocStream = this.javaClass.classLoader.getResourceAsStream(
      SEMANTIC_DATA_ROOT + "/documents/completion/"
        + sectionName + "/" + keyName + ".html"
    ) ?: return getDeprecationReason(sectionName, keyName, true)
    try {
      return IOUtils.toString(htmlDocStream, StandardCharsets.UTF_8)
    } catch (e: IOException) {
      LOG.warn("Could not convert html document stream to String", e)
    }
    return null
  }

  /**
   * Returns the reason for a deprecation for a specific section / keyword.
   * @param sectionName section name
   * @param keyName key name
   * @param html whether the result should be in html
   * @return deprecation reason
   */
  fun getDeprecationReason(sectionName: String, keyName: String, html: Boolean): String? {
    val options = getKeyValuePairsForSectionFromUndocumentedInformation(sectionName)[keyName] ?: return null
    return when (options.reason) {
      "unsupported" -> {
        if (!html) String.format("'%s' in section '%s' is not officially supported", keyName, sectionName) else ("<p><var>" + keyName + "</var> in section <b>" + sectionName + "</b> is not officially supported.<p>"
          + "<a href='" + options.documentationLink + "'>More information is available here</a>")
      }

      "moved" -> {
        val newKeyName = StringUtil.notNullize(options.replacedWithKey, keyName)
        val newSectionName = StringUtil.notNullize(options.replacedWithSection, sectionName)
        val semanticsNote = if (!html) "" else ("<p>NOTE: The semantics of the new value may not match the existing value.<p>"
          + "<a href='" + options.documentationLink + "'>More information is available here</a>")
        if (newSectionName == sectionName) {
          LOG.assertTrue(newKeyName != keyName, String.format("Meaningless move/rename of %s.%s", sectionName, keyName))
          return if (!html) {
            String.format("'%s' in section '%s' has been renamed to '%s'", keyName, sectionName, newKeyName)
          } else ("<p>The key <var>" + keyName + "</var> in section <b>" + sectionName + "</b> has been renamed to "
            + "<var>" + newKeyName + "</var>" + semanticsNote)
        }
        if (newKeyName == keyName) {
          return if (!html) {
            String.format("'%s' has been moved to section '%s'", keyName, newSectionName)
          } else ("<p>The key <var>" + keyName + "</var> in section <b>" + sectionName + "</b> has been moved to "
            + "section <b>" + newSectionName + "</b>" + semanticsNote)
        }
        if (!html) {
          String.format("'%s' in section '%s' has been moved to '%s' in section '%s'", keyName, sectionName, newKeyName, newSectionName)
        } else ("<p>The key <var>" + keyName + "</var> in section <b>" + sectionName + "</b> has been moved to "
          + "<var>" + newKeyName + "</var> in section <b>" + newSectionName + "</b>"
          + semanticsNote)
      }

      "manual" -> {
        if (!html) StringUtil.stripHtml(options.description!!, false) else options.description
      }

      else -> {
        LOG.warn("Found deprecated key '" + sectionName + "." + keyName + "' with unclear reason: " + options.reason)
        null
      }
    }
  }

  /**
   * Checks whether the option is deprecated or unsupported.
   *
   * @param sectionName the section name to lookup (e.g., Unit, Install, Service)
   * @param keyName the key name to look up.
   * @return true if we know the option is deprecated
   */
  fun isDeprecated(sectionName: String, keyName: String): Boolean {
    val options = getKeyValuePairsForSectionFromUndocumentedInformation(sectionName)[keyName]
    return options != null
  }

  /**
   * Gets the validator for a section name and key name.
   *
   * @param sectionName the name of the section we are looking up
   * @param keyName - the keyname to look up
   * @return the validator
   */
  fun getOptionValidator(sectionName: String, keyName: String): OptionValueInformation {
    return validatorMap.get(getValidatorForSectionAndKey(sectionName, keyName)) ?: NullOptionValue.INSTANCE
  }


  fun getValidatorMap() : Map<Validator, OptionValueInformation> = validatorMap

  val sectionNamesFromValidators: Set<String>
    /**
     * Return the section names from the validators.
     *
     * @return set
     */
    get() = Collections.unmodifiableSet(sectionToKeyAndValidatorMap.keys)


  fun getValidatorForSectionAndKey(sectionName: String, keyName: String) : Validator {
    var validatorKey = sectionToKeyAndValidatorMap.getOrDefault(sectionName, emptyMap())[keyName]
    if (sectionName.trim { it <= ' ' } == "Install") {
      if (keyName.trim { it <= ' ' } == "WantedBy" || keyName.trim { it <= ' ' } == "Also" || keyName.trim { it <= ' ' } == "RequiredBy") {
        // Override the validators for these cases since there is no validation done now, but really they should be units.
        validatorKey = Validator("config_parse_unit_deps", "0")
      }
    }

    if (validatorKey is Validator) {
      if (!validatorMap.containsKey(validatorKey)) {
        val wildcardValidator = validatorKey.copy(validatorArgument = "*")

        if (validatorMap.containsKey(wildcardValidator)) {
            return wildcardValidator
          }
      }

      return validatorKey
    }

    return NullOptionValue.VALIDATOR_INSTANCE
  }
  /**
   * Return the section names from the validators.
   *
   * @return set
   */
  fun getAllowedKeywordsInSectionFromValidators(sectionName: String?): Set<String> {
    return Collections.unmodifiableSet(sectionToKeyAndValidatorMap.getOrDefault(sectionName, emptyMap()).keys)
  }

  fun getAllowedSectionsInFile(fileName: String): Set<String> {

    val completeSections = when(getUnitType(fileName)) {
      "Automount" ->  setOf("Unit", "Install", "Automount")
      "Device", "Target" -> setOf("Unit", "Install")
      "Mount" ->  setOf("Unit", "Install", "Mount")
      "Path" -> setOf("Unit", "Install", "Path")
      "Service" -> setOf("Unit", "Install", "Service")
      "Slice" -> setOf("Unit", "Install", "Slice")
      "Socket" -> setOf("Unit", "Install", "Socket")
      "Swap" ->  setOf("Unit", "Install", "Swap")
      "Timer" -> setOf("Unit", "Install", "Timer")
      else -> setOf()
    }

    return completeSections
  }

  fun getUnitType(fileName: String): String {
    return fileName.substringAfterLast(".", "").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
  }

  fun getRequiredKeys(containingFile: String): Set<String> {
    val unitType = getUnitType(containingFile)

    return when(unitType) {
      "Service" ->
       setOf("Unit.SuccessAction", "Service.ExecStart", "Service.ExecStop")
      "Path" ->
        setOf("Path.PathExists", "Path.PathExistsGlob", "Path.PathChanged", "Path.PathModified", "Path.DirectoryNotEmpty")
      "Timer" ->
        setOf("Timer.OnActiveSec", "Timer.OnBootSec", "Timer.OnStartupSec", "Timer.OnUnitActiveSec", "Timer.OnUnitInactiveSec", "Timer.OnCalendar", "Timer.OnClockChange", "Timer.OnTimezoneChange")
      "Swap" ->
        setOf("Swap.What")
      "Mount" ->
        // Both of these are required, but we only error out on one :(
        // systemd-analyze only complains about what though, so ... we can ignore it until a bug comes.
        setOf("Mount.What", "Mount.Where")
      "Socket" ->
        setOf("Socket.ListenStream", "Socket.ListenDatagram", "Socket.ListenSequentialPacket", "Socket.ListenFIFO", "Socket.ListenSpecial", "Socket.ListenNetlink", "Socket.ListenMessageQueue", "Socket.ListenUSBFunction")
      "Automount" ->
        setOf("Automount.Where")
      else -> setOf()
    }

  }

  companion object {
    private val LOG = Logger.getInstance(SemanticDataRepository::class.java)
    const val SEMANTIC_DATA_ROOT = "net/sjrx/intellij/plugins/systemdunitfiles/semanticdata/"
    private const val GPERF_REGEX = "^(?<Section>[A-Z][a-z]+).(?<Key>\\w+),\\s*(?<Validator>\\w+),\\s*(?<MysteryColumn>\\w+)\\s*,.+$"
    private val LINE_MATCHER = Pattern.compile(GPERF_REGEX)
    private val NULL_VALIDATOR = Validator("NULL", "0")

    @JvmStatic
    val lazyInstance = NotNullLazyValue.atomicLazy { SemanticDataRepository() }

    val instance: SemanticDataRepository
      get() = lazyInstance.get()

    private const val SCOPE_KEYWORD = "Scope"
    private const val LEGACY_PARAMETERS_KEY = "DISABLED_LEGACY"
    private const val EXPERIMENTAL_PARAMETERS_KEY = "DISABLED_EXPERIMENTAL"
    private fun <K> intern(cache: Map<K, K>, value: K): K {
      return cache[value] ?: value
    }
  }
}

data class Validator(val validatorName: String, val validatorArgument: String) {
  override fun toString() = "${validatorName}(${validatorArgument})"
}
