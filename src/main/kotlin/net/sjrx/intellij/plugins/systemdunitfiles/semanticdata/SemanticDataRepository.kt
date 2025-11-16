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
import com.intellij.psi.PsiFile
import com.intellij.util.ObjectUtils
import net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.*
import org.apache.commons.io.IOUtils
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern

enum class FileClass(val fileClass: String, val gperfFile: String) {
  UNIT_FILE("unit", "load-fragment-gperf.gperf"),
  NSPAWN("nspawn", "nspawn-gperf.gperf"),
  NETDEV("netdev", "netdev-gperf.gperf"),
  NETWORK("network", "networkd-network-gperf.gperf"),
  LINK("link", "link-config-gperf.gperf")
}


fun PsiFile.fileClass(): FileClass {
  return SemanticDataRepository.getFileClassForFilename(this.name)
}

class SemanticDataRepository private constructor() {
  private val validatorMap: Map<Validator, OptionValueInformation>

  private val fileClassToSectionToKeyAndValidatorMap: MutableMap<String, MutableMap<String, MutableMap<String, Validator>>> = HashMap()

  private val fileClassToSectionNameToKeyValuesFromDoc: MutableMap<String, MutableMap<String, Map<String, KeywordData>>>

  private val fileClassToUndocumentedOptionInfo: MutableMap<String, MutableMap<String, Map<String, KeywordData>>>

  private val fileClassToValidatorConfigToSectionKeys: MutableMap<String, MutableMap<String, MutableSet<String>>> = HashMap()

  private val fileClassToSectionKeyToValidatorConfig: MutableMap<String, MutableMap<String, String>> = HashMap()

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

    fileClassToSectionNameToKeyValuesFromDoc = loadSemanticDataFromJsonFile(SEMANTIC_DATA_ROOT + "sectionToKeywordMapFromDoc.json", cache)
    fileClassToUndocumentedOptionInfo = loadSemanticDataFromJsonFile(SEMANTIC_DATA_ROOT + "undocumentedSectionToKeywordMap.json", cache)


    for (fileClass in FileClass.entries) {
      val name = SEMANTIC_DATA_ROOT + fileClass.gperfFile
      val resource = this.javaClass.classLoader.getResourceAsStream(name)
      fileClassToValidatorConfigToSectionKeys.put(fileClass.fileClass, HashMap())
      fileClassToSectionKeyToValidatorConfig.put(fileClass.fileClass, HashMap())

      if (resource != null) {
        try {
          BufferedReader(InputStreamReader(resource)).use { fr ->
            var line: String?
            while (fr.readLine().also { line = it } != null) {
              val lineWithoutComments = line?.replace(Regex("/\\*.*?\\*/"), "")
              val m = LINE_MATCHER.matcher(lineWithoutComments)
              if (m.find()) {
                val section = m.group("Section")
                val key = m.group("Key")
                val validator = m.group("Validator")
                val mysteryValue = m.group("MysteryColumn1")
                val mysteryValue2 = m.group("MysteryColumn2")
                when (mysteryValue) {
                  LEGACY_PARAMETERS_KEY, EXPERIMENTAL_PARAMETERS_KEY -> {}


                  else -> {
                    val myValidator = Validator(validator, mysteryValue)
                    fileClassToSectionToKeyAndValidatorMap.computeIfAbsent(fileClass.fileClass, { _: String? -> HashMap() }).computeIfAbsent(intern(cache, section)) { _: String? -> HashMap() }[intern(cache, key)] = intern(cache2, myValidator)
                  }
                }

                if (mysteryValue2 != "0") {

                  val validatorConfigKey = mysteryValue + "/" + mysteryValue2
                  val validatorConfigToSectionKey = fileClassToValidatorConfigToSectionKeys.get(fileClass.fileClass)
                  validatorConfigToSectionKey?.putIfAbsent(validatorConfigKey, HashSet())

                  val set = validatorConfigToSectionKey?.get(validatorConfigKey)

                  set?.add("$section.$key")

                  val sectionKeyToValidatorConfig = fileClassToSectionKeyToValidatorConfig.get(fileClass.fileClass)

                  sectionKeyToValidatorConfig?.put("$section.$key", validatorConfigKey)
                }

              }
            }
          }
        } catch (e: IOException) {
          LOG.error("Unable to initialize data for systemd inspections plugin", e)
        }
      } else {
        LOG.error("Unable to initialize data for systemd inspections plugin, resource $name not found")
      }
    }


    validatorMap = HashMap()
    validatorMap.putAll(BooleanOptionValue.validators)
    validatorMap.putAll(TriStateOptionValue.validators)
    validatorMap.putAll(DocumentationOptionValue.validators)
    validatorMap.putAll(ModeStringOptionValue.validators)
    validatorMap.putAll(ExecOptionValue.validators)
    validatorMap.putAll(ExecOutputOptionValue.validators)
    validatorMap.putAll(UnitDependencyOptionValue.validators)
    validatorMap.putAll(NullOptionValue.validators)
    validatorMap.putAll(MemoryLimitOptionValue.validators)
    validatorMap.putAll(SignalOptionValue.validators)
    validatorMap.putAll(NamespacePathOptionValue.validators)
    validatorMap.putAll(UnsignedIntegerOptionValue.validators)
    validatorMap.putAll(PathOptionValue.validators)
    validatorMap.putAll(EnumOptionValues.validators)
    validatorMap.putAll(ConfigParseSecValidators.validators)
    validatorMap.putAll(AllowedCpuSetOptionValue.validators)
    validatorMap.putAll(TtySizeOptionValue.validators)
    validatorMap.putAll(ExecDirectoriesOptionValue.validators)
    validatorMap.putAll(IOLimitOptionValue.validators)
    validatorMap.putAll(CGWeightOptionValue.validators)
    validatorMap.putAll(BlockIOWeightOptionValue.validators)
    validatorMap.putAll(BlockIOBandwidthOptionValue.validators)
    validatorMap.putAll(ImagePolicyOptionValue.validators)
    validatorMap.putAll(CPUWeightOptionValue.validators)
    validatorMap.putAll(CPUSharesOptionValue.validators)
    validatorMap.putAll(CgroupSocketBindOptionValue.validators)
    validatorMap.putAll(RlimitOptionValue.validators) // Scopes are not supported since they aren't standard unit files.
    validatorMap.putAll(NetworkAddressOptionValue.validators)
    validatorMap.putAll(InAddrPrefixesOptionValue.validators)
    validatorMap.putAll(UIntOptionValues.validators)
    validatorMap.putAll(SimpleGrammarOptionValues.validators)
    validatorMap.putAll(getAllAIGeneratedValidators())
    fileClassToSectionNameToKeyValuesFromDoc["unit"]?.remove(SCOPE_KEYWORD)
    fileClassToSectionToKeyAndValidatorMap["unit"]?.remove(SCOPE_KEYWORD)
  }

  private fun loadSemanticDataFromJsonFile(filename: String, cache: Map<String, String>): MutableMap<String, MutableMap<String, Map<String, KeywordData>>> {
    val sectionToKeywordMapFromDocJsonFile = this.javaClass.classLoader.getResource(filename)
    val factory = JsonFactoryBuilder().disable(JsonFactory.Feature.INTERN_FIELD_NAMES).build()
    if (!ApplicationManager.getApplication().isUnitTestMode) {
      factory.disable(JsonParser.Feature.IGNORE_UNDEFINED)
    }

    val mapper = ObjectMapper(factory)
    mapper.registerModule(KotlinModule.Builder().build())

    try {
      val read: Map<String, Map<String, Map<String, KeywordData>>> = mapper.readValue(sectionToKeywordMapFromDocJsonFile)

      // intern strings in maps
      val output: MutableMap<String, MutableMap<String, Map<String, KeywordData>>> = HashMap()

      read.forEach { (fileClass: String, m0: Map<String, Map<String, KeywordData>>) ->
        val fileClassMap = HashMap<String, Map<String, KeywordData>>()
        output[fileClass] = fileClassMap


        m0.forEach { (section: String, m1: Map<String, KeywordData>) ->
          val converted = HashMap<String, KeywordData>()
          m1.forEach { (keyword: String, m2: KeywordData) -> converted[intern(cache, keyword)] = m2 }
          fileClassMap[intern(cache, section)] = converted
        }
      }
      return output

    } catch (e: IOException) {
      LOG.error("Unable to initialize data for systemd inspections plugin", e)
      return HashMap()


    }
  }

  fun getSectionNamesForFile(fileClass: String): Set<String> {
    return Collections.unmodifiableSet(fileClassToSectionNameToKeyValuesFromDoc.getOrDefault(fileClass, emptyMap()).keys)
  }

  fun getAliasesForSectionKey(fileClass: String, sectionKey: String): Set<String> {

    val validatorConfig = fileClassToSectionKeyToValidatorConfig.getOrDefault(fileClass, emptyMap())[sectionKey]

    if (validatorConfig == null) {
      return emptySet()
    }

    return Collections.unmodifiableSet(fileClassToValidatorConfigToSectionKeys.getOrDefault(fileClass, emptyMap()).getOrDefault(validatorConfig, emptySet()))
  }

  /**
   * Returns the allowed keywords by a given section name.
   *
   * @param section the section name (e.g., Unit, Install, Service)
   * @return set of allowed names.
   */
  fun getDocumentedKeywordsInSection(fileclass: FileClass, section: String): Set<String> {
    val keys: MutableSet<String> = HashSet(getKeyValuePairsForSectionFromDocumentation(fileclass, section).keys)
    keys.addAll(getKeyValuePairsForSectionFromUndocumentedInformation(fileclass, section).keys)
    return Collections.unmodifiableSet(keys)
  }

  /**
   * Returns the documentation url for a section and keyname.
   *
   * @param section - section name
   * @param keyName - key name
   * @return String or null
   */
  fun getKeywordDocumentationUrl(fileclass: FileClass, section: String, keyName: String): String? {
    var data = getKeyValuePairsForSectionFromDocumentation(fileclass, section)[keyName]
    if (data != null && data.documentationLink != null) {
      return data.documentationLink
    }
    data = getKeyValuePairsForSectionFromUndocumentedInformation(fileclass, section)[keyName]
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
  fun getKeywordLocationInDocumentation(fileclass: FileClass, section: String, keyName: String): String? {
    val data = getKeyValuePairsForSectionFromDocumentation(fileclass, section)[keyName] ?: return null
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
  fun getKeywordFileLocationInDocumentation(fileclass: FileClass, section: String, keyName: String): String? {
    val data = getKeyValuePairsForSectionFromDocumentation(fileclass, section)[keyName]
    return data?.declaredInFile
  }

  fun getKeyValuePairsForSectionFromDocumentation(fileClass: FileClass, section: String): Map<String, KeywordData> {
    return fileClassToSectionNameToKeyValuesFromDoc[fileClass.fileClass]?.get(section) ?: emptyMap()
  }

  fun getKeyValuePairsForSectionFromUndocumentedInformation(fileClass: FileClass, section: String): Map<String, KeywordData> {
    return fileClassToUndocumentedOptionInfo[fileClass.fileClass]?.get(section) ?: emptyMap()
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
      "Exec" -> "https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#%5BExec%5D%20Section%20Options"
      "Files" -> "https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#%5BFiles%5D%20Section%20Options"
      "Network" -> "https://www.freedesktop.org/software/systemd/man/latest/systemd.nspawn.html#%5BNetwork%5D%20Section%20Options"
      // network files
      // cat systemd.network.xml | grep "Section Options"   | sed -E 's/^.+\[(.+)\].+$/"\1" -> """https:\/\/www.freedesktop.org\/software\/systemd\/man\/latest\/systemd.network.html#%5B\1%5D%20Section%20Options"""/g' | sort
      "Address" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BAddress%5D%20Section%20Options"""
      "BandMultiQueueing" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BBandMultiQueueing%5D%20Section%20Options"""
      "BFIFO" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BBFIFO%5D%20Section%20Options"""
      "BridgeFDB" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BBridgeFDB%5D%20Section%20Options"""
      "Bridge" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BBridge%5D%20Section%20Options"""
      "BridgeMDB" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BBridgeMDB%5D%20Section%20Options"""
      "BridgeVLAN" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BBridgeVLAN%5D%20Section%20Options"""
      "CAKE" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BCAKE%5D%20Section%20Options"""
      "CAN" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BCAN%5D%20Section%20Options"""
      "ClassfulMultiQueueing" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BClassfulMultiQueueing%5D%20Section%20Options"""
      "ControlledDelay" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BControlledDelay%5D%20Section%20Options"""
      "DeficitRoundRobinSchedulerClass" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BDeficitRoundRobinSchedulerClass%5D%20Section%20Options"""
      "DeficitRoundRobinScheduler" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BDeficitRoundRobinScheduler%5D%20Section%20Options"""
      "DHCPPrefixDelegation" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BDHCPPrefixDelegation%5D%20Section%20Options"""
      "DHCPServer" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BDHCPServer%5D%20Section%20Options"""
      "DHCPServerStaticLease" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BDHCPServerStaticLease%5D%20Section%20Options"""
      "DHCPv4" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BDHCPv4%5D%20Section%20Options"""
      "DHCPv6" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BDHCPv6%5D%20Section%20Options"""
      "EnhancedTransmissionSelection" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BEnhancedTransmissionSelection%5D%20Section%20Options"""
      "FairQueueingControlledDelay" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BFairQueueingControlledDelay%5D%20Section%20Options"""
      "FairQueueing" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BFairQueueing%5D%20Section%20Options"""
      "FlowQueuePIE" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BFlowQueuePIE%5D%20Section%20Options"""
      "GenericRandomEarlyDetection" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BGenericRandomEarlyDetection%5D%20Section%20Options"""
      "HeavyHitterFilter" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BHeavyHitterFilter%5D%20Section%20Options"""
      "HierarchyTokenBucketClass" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BHierarchyTokenBucketClass%5D%20Section%20Options"""
      "HierarchyTokenBucket" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BHierarchyTokenBucket%5D%20Section%20Options"""
      "IPoIB" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BIPoIB%5D%20Section%20Options"""
      "IPv6AcceptRA" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BIPv6AcceptRA%5D%20Section%20Options"""
      "IPv6AddressLabel" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BIPv6AddressLabel%5D%20Section%20Options"""
      "IPv6PREF64Prefix" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BIPv6PREF64Prefix%5D%20Section%20Options"""
      "IPv6Prefix" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BIPv6Prefix%5D%20Section%20Options"""
      "IPv6RoutePrefix" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BIPv6RoutePrefix%5D%20Section%20Options"""
      "IPv6SendRA" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BIPv6SendRA%5D%20Section%20Options"""
      "Link" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BLink%5D%20Section%20Options"""
      "LLDP" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BLLDP%5D%20Section%20Options"""
      "Match" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BMatch%5D%20Section%20Options"""
      "Neighbor" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BNeighbor%5D%20Section%20Options"""
      "NetworkEmulator" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BNetworkEmulator%5D%20Section%20Options"""
      //"Network" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BNetwork%5D%20Section%20Options"""
      "NextHop" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BNextHop%5D%20Section%20Options"""
      "PFIFOFast" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BPFIFOFast%5D%20Section%20Options"""
      "PFIFOHeadDrop" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BPFIFOHeadDrop%5D%20Section%20Options"""
      "PFIFO" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BPFIFO%5D%20Section%20Options"""
      "PIE" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BPIE%5D%20Section%20Options"""
      "QDisc" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BQDisc%5D%20Section%20Options"""
      "QuickFairQueueingClass" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BQuickFairQueueingClass%5D%20Section%20Options"""
      "QuickFairQueueing" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BQuickFairQueueing%5D%20Section%20Options"""
      "Route" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BRoute%5D%20Section%20Options"""
      "RoutingPolicyRule" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BRoutingPolicyRule%5D%20Section%20Options"""
      "StochasticFairBlue" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BStochasticFairBlue%5D%20Section%20Options"""
      "StochasticFairnessQueueing" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BStochasticFairnessQueueing%5D%20Section%20Options"""
      "TokenBucketFilter" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BTokenBucketFilter%5D%20Section%20Options"""
      "TrivialLinkEqualizer" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.network.html#%5BTrivialLinkEqualizer%5D%20Section%20Options"""
      // Netdev
      "BareUDP" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BBareUDP%5D%20Section%20Options"""
      "BatmanAdvanced" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BBatmanAdvanced%5D%20Section%20Options"""
      "Bond" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BBond%5D%20Section%20Options"""
      //"Bridge" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BBridge%5D%20Section%20Options"""
      "FooOverUDP" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BFooOverUDP%5D%20Section%20Options"""
      "GENEVE" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BGENEVE%5D%20Section%20Options"""
      //"IPoIB" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BIPoIB%5D%20Section%20Options"""
      "IPVLAN" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BIPVLAN%5D%20Section%20Options"""
      "IPVTAP" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BIPVTAP%5D%20Section%20Options"""
      "L2TP" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BL2TP%5D%20Section%20Options"""
      "L2TPSession" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BL2TPSession%5D%20Section%20Options"""
      "MACsec" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BMACsec%5D%20Section%20Options"""
      "MACsecReceiveAssociation" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BMACsecReceiveAssociation%5D%20Section%20Options"""
      "MACsecReceiveChannel" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BMACsecReceiveChannel%5D%20Section%20Options"""
      "MACsecTransmitAssociation" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BMACsecTransmitAssociation%5D%20Section%20Options"""
      "MACVLAN" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BMACVLAN%5D%20Section%20Options"""
      "MACVTAP" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BMACVTAP%5D%20Section%20Options"""
      //"Match" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BMatch%5D%20Section%20Options"""
      "NetDev" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BNetDev%5D%20Section%20Options"""
      "Peer" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BPeer%5D%20Section%20Options"""
      "Tap" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BTap%5D%20Section%20Options"""
      "Tun" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BTun%5D%20Section%20Options"""
      "Tunnel" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BTunnel%5D%20Section%20Options"""
      "VLAN" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BVLAN%5D%20Section%20Options"""
      "VRF" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BVRF%5D%20Section%20Options"""
      "VXCAN" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BVXCAN%5D%20Section%20Options"""
      "VXLAN" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BVXLAN%5D%20Section%20Options"""
      "WireGuard" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BWireGuard%5D%20Section%20Options"""
      "WireGuardPeer" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BWireGuardPeer%5D%20Section%20Options"""
      "WLAN" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BWLAN%5D%20Section%20Options"""
      "Xfrm" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.netdev.html#%5BXfrm%5D%20Section%20Options"""

      // link
      //"Link" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#%5BLink%5D%20Section%20Options"""
      //"Match" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#%5BMatch%5D%20Section%20Options"""
      "SR-IOV" -> """https://www.freedesktop.org/software/systemd/man/latest/systemd.link.html#%5BSR-IOV%5D%20Section%20Options"""

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

      "Timer" -> """Timer files must include a [Timer] section, which carries information
 about the timer it defines."""

      "Unit" -> """The unit file may include a [Unit] section, which carries generic
 information about the unit that is not dependent on the type of unit."""

      // nspawn
      "Exec" -> """The [Exec] section controls various execution parameters."""
      "Files" -> """The [Files] section, carries various parameters configuring the filesystem of the container."""
      "Network" -> """The [Network] section carries various parameters configuring the network connectivity of the container."""

      // netdev
      "BareUDP" -> """The [BareUDP] section applies only for netdevs of kind "bareudp". Bare UDP tunnels provide a generic L3 encapsulation support for tunnelling different L3 protocols like MPLS, IP etc. inside of a UDP tunnel."""
      "BatmanAdvanced" -> """The [BatmanAdvanced] section applies only for netdevs of kind "batadv". <a href="https://www.open-mesh.org/projects/open-mesh/wiki">B.A.T.M.A.N. Advanced</a> is a routing protocol for multi-hop mobile ad-hoc networks which operates on layer 2."""
      "Bond" -> """The [Bond] section applies only for netdevs of kind "bond". A bond device is an aggregation of all its slave devices. See <a href="https://docs.kernel.org/networking/bonding.html">Linux Ethernet Bonding Driver HOWTO</a> for details. ... ."""
      "Bridge" -> """The [Bridge] section applies only for netdevs of kind "bridge". A bridge device is a software switch, and each of its slave devices and the bridge itself are ports of the switch."""
      "FooOverUDP" -> """The [FooOverUDP] section applies only for netdevs of kind "fou". See <a href="https://lwn.net/Articles/614348/">Foo over UDP</a> for more details."""
      "GENEVE" -> """The [GENEVE] section applies only for netdevs of kind "geneve". GENEVE (GEneric NEtwork Virtualization Encapsulation) is a flexible tunneling protocol used in virtualized networking environments. See <a href="https://datatracker.ietf.org/doc/html/rfc8926">RFC 8926</a> for details."""
      "IPoIB" -> """The [IPoIB] section applies only for netdevs of kind "ipoib". IP over InfiniBand (IPoIB) enables IP networking over high-performance InfiniBand fabric."""
      "IPVLAN" -> """The [IPVLAN] section applies only for netdevs of kind "ipvlan". IPVLAN devices allow multiple IP addresses on a single physical interface while sharing a MAC address, reducing broadcast domain size."""
      "IPVTAP" -> """The [IPVTAP] section applies only for netdevs of kind "ipvtap". Similar to IPVLAN but integrates with the tap interface, allowing userspace applications to receive traffic."""
      "L2TPSession" -> """The [L2TPSession] section applies only for netdevs of kind "l2tp". Defines Layer 2 Tunneling Protocol (L2TP) session parameters, used to create VPNs or encapsulate other protocols."""
      "L2TP" -> """The [L2TP] section applies only for netdevs of kind "l2tp". Configures L2TP tunnels, which are used for VPNs and ISP service delivery. L2TP itself does not provide encryption."""
      "MACsecReceiveAssociation" -> """The [MACsecReceiveAssociation] section applies only for netdevs of kind "macsec". Defines receive-side security association settings for MACsec-secured Ethernet links."""
      "MACsecReceiveChannel" -> """The [MACsecReceiveChannel] section applies only for netdevs of kind "macsec". Configures reception channels for MACsec-secured communication over Ethernet links."""
      "MACsec" -> """The [MACsec] section applies only for netdevs of kind "macsec". MACsec (IEEE 802.1AE) provides encryption and authentication for Ethernet frames at the link layer."""
      "MACsecTransmitAssociation" -> """The [MACsecTransmitAssociation] section applies only for netdevs of kind "macsec". Defines transmit-side security association settings for MACsec-protected links."""
      "MACVLAN" -> """The [MACVLAN] section applies only for netdevs of kind "macvlan". MACVLAN allows multiple MAC addresses to be assigned to a single physical interface, enabling network isolation."""
      "MACVTAP" -> """The [MACVTAP] section applies only for netdevs of kind "macvtap". MACVTAP extends MACVLAN with a tap interface, allowing userspace applications to interact with the virtual interface."""
      "Match" -> """The [Match] section is used to filter which network devices a .netdev file should apply to, based on criteria such as interface name, MAC address, or driver."""
      "NetDev" -> """The [NetDev] section defines a virtual network device. Each .netdev file describes a single virtual network device and its properties."""
      "Peer" -> """The [Peer] section applies only for netdevs of kind "veth". Defines the peer interface of a veth (virtual Ethernet) tunnel, which is commonly used to connect network namespaces."""
      "Tap" -> """The [Tap] section applies only for netdevs of kind "tap". A TAP device is a virtual network interface that operates at Layer 2, used for bridging and tunneling."""
      "Tunnel" -> """The [Tunnel] section applies only for netdevs of tunneling kinds (e.g., "ipip", "sit", "gre", "gretap", "ip6gre", "ip6gretap", "vti", "vti6", "ip6tnl", and "erspan"). Configures tunneling parameters for encapsulating traffic over another network layer."""
      "Tun" -> """The [Tun] section applies only for netdevs of kind "tun". A TUN device is a virtual network interface that operates at Layer 3, used for IP tunneling."""
      "VLAN" -> """The [VLAN] section applies only for netdevs of kind "vlan". VLAN interfaces allow traffic segmentation using IEEE 802.1Q tagging. See <a href="http://www.ieee802.org/1/pages/802.1Q.html">IEEE 802.1Q</a>."""
      "VRF" -> """The [VRF] section applies only for netdevs of kind "vrf". A Virtual Routing and Forwarding (VRF) interface provides separate routing domains within a system. See <a href="https://docs.kernel.org/networking/vrf.html">VRF documentation</a>."""
      "VXCAN" -> """The [VXCAN] section applies only for netdevs of kind "vxcan". VXCAN is a virtual CAN tunnel, similar to veth, creating a peer-to-peer link between namespaces for CAN traffic."""
      "VXLAN" -> """The [VXLAN] section applies only for netdevs of kind "vxlan". VXLAN (Virtual eXtensible LAN) is a tunneling protocol for overlay networks, commonly used in cloud and datacenter environments."""
      "WireGuardPeer" -> """The [WireGuardPeer] section applies only for netdevs of kind "wireguard". Defines peer-specific settings for WireGuard tunnels, such as allowed IPs and public keys."""
      "WireGuard" -> """The [WireGuard] section applies only for netdevs of kind "wireguard". WireGuard is a modern, secure VPN protocol designed for simplicity and high performance."""
      "WLAN" -> """The [WLAN] section applies only for netdevs of kind "wlan". Configures a virtual wireless LAN (WLAN) interface, useful for virtualized network environments."""
      "Xfrm" -> """The [Xfrm] section applies only for netdevs of kind "xfrm". Configures XFRM-based virtual interfaces, which handle IPsec and other transformation-based networking policies."""
      "Link" -> """The [Link] section specifies how the link should be configured. ."""
      "SR-IOV" -> """The [SR-IOV] section specifies how to configure several Single Root I/O Virtualization (SR-IOV)."""
      // network files
      //  cat systemd.network.xml | grep "Section Options"   | sed -E 's/^.+\[(.+)\].+$/"\1" -> """The [\1] section ... """ /g' | sort
      "Address" -> """The [Address] section defines IP addresses assigned to the interface. It supports settings such as the main address, peer address, and broadcast address configuration."""
      "BandMultiQueueing" -> """The [BandMultiQueueing] section manages the queueing discipline (qdisc) of Band Multi Queueing (multiq)."""
      "BFIFO" -> """The [BFIFO] section manages the queueing discipline (qdisc) of Byte limited Packet First In First Out (bfifo). It sets limits on the FIFO buffer size in bytes."""
      "BridgeFDB" -> """The [BridgeFDB] section manages the forwarding database table of a port, allowing static MAC table entries for bridges."""
      "BridgeMDB" -> """The [BridgeMDB] section configures multicast membership entries in the bridge multicast database (MDB), enabling multicast group management."""
      "BridgeVLAN" -> """The [BridgeVLAN] section manages VLAN ID configurations for a bridge, requiring VLAN filtering to be enabled."""
      "CAKE" -> """The [CAKE] section manages the queueing discipline of Common Applications Kept Enhanced (CAKE), a QoS system that improves bandwidth fairness and latency."""
      "CAN" -> """The [CAN] section configures Controller Area Network (CAN bus) settings, including bitrate and sample points."""
      "ClassfulMultiQueueing" -> """The [ClassfulMultiQueueing] section manages the queueing discipline (qdisc) of Classful Multi Queueing (mq)."""
      "ControlledDelay" -> """The [ControlledDelay] section manages the queueing discipline (qdisc) of Controlled Delay (CoDel), used for active queue management."""
      "DeficitRoundRobinSchedulerClass" -> """The [DeficitRoundRobinSchedulerClass] section manages the traffic control class of Deficit Round Robin Scheduler (DRR)."""
      "DeficitRoundRobinScheduler" -> """The [DeficitRoundRobinScheduler] section manages the queueing discipline (qdisc) of Deficit Round Robin Scheduler (DRR)."""
      "DHCPPrefixDelegation" -> """The [DHCPPrefixDelegation] section configures IPv6 prefix delegation settings for DHCPv6."""
      "DHCPServerStaticLease" -> """The [DHCPServerStaticLease] section configures static DHCP leases, assigning a fixed IPv4 address to a MAC address."""
      "DHCPServer" -> """The [DHCPServer] section configures the DHCPv4 server, allowing settings such as address pools and lease times."""
      "DHCPv4" -> """The [DHCPv4] section configures the DHCPv4 client, supporting options such as IAID, DUID, and requested DHCP options."""
      "DHCPv6" -> """The [DHCPv6] section configures the DHCPv6 client, controlling settings for IPv6 address assignment."""
      "EnhancedTransmissionSelection" -> """The [EnhancedTransmissionSelection] section manages the queueing discipline (qdisc) of Enhanced Transmission Selection (ETS)."""
      "FairQueueingControlledDelay" -> """The [FairQueueingControlledDelay] section manages the queueing discipline of FQ-CoDel, a fair queueing version of CoDel."""
      "FairQueueing" -> """The [FairQueueing] section manages fair queueing algorithms, ensuring balanced traffic distribution."""
      "FlowQueuePIE" -> """The [FlowQueuePIE] section configures the queueing discipline (qdisc) of Flow Queue Proportional Integral controller-Enhanced (fq_pie)."""
      "GenericRandomEarlyDetection" -> """The [GenericRandomEarlyDetection] section configures the Random Early Detection (RED) algorithm for congestion control."""
      "HeavyHitterFilter" -> """The [HeavyHitterFilter] section configures the queueing discipline (qdisc) of Heavy Hitter Filter (hhf)."""
      "HierarchyTokenBucketClass" -> """The [HierarchyTokenBucketClass] section manages the traffic control class of Hierarchy Token Bucket (htb)."""
      "HierarchyTokenBucket" -> """The [HierarchyTokenBucket] section manages the queueing discipline (qdisc) of Hierarchy Token Bucket (htb)."""
      "IPv6AcceptRA" -> """The [IPv6AcceptRA] section configures the reception of IPv6 Router Advertisements (RA) for autoconfiguration."""
      "IPv6AddressLabel" -> """The [IPv6AddressLabel] section configures IPv6 address labels for precedence in address selection."""
      "IPv6PREF64Prefix" -> """The [IPv6PREF64Prefix] section configures IPv6 PREF64 (NAT64) prefixes for translation."""
      "IPv6Prefix" -> """The [IPv6Prefix] section defines IPv6 prefixes for network configuration."""
      "IPv6RoutePrefix" -> """The [IPv6RoutePrefix] section defines IPv6 route prefixes distributed via Router Advertisements."""
      "IPv6SendRA" -> """The [IPv6SendRA] section configures IPv6 Router Advertisement (RA) sending settings."""
      "LLDP" -> """The [LLDP] section manages Link Layer Discovery Protocol (LLDP) settings, including MUD URLs."""
      "Neighbor" -> """The [Neighbor] section configures static neighbor entries in the ARP or IPv6 neighbor table."""
      "NetworkEmulator" -> """The [NetworkEmulator] section configures a network emulation qdisc, allowing simulation of delays and packet loss."""
      "NextHop" -> """The [NextHop] section defines next-hop entries for routing configuration."""
      "PFIFOFast" -> """The [PFIFOFast] section manages the queueing discipline (qdisc) of Packet First In First Out Fast (pfifo_fast)."""
      "PFIFOHeadDrop" -> """The [PFIFOHeadDrop] section manages the queueing discipline (qdisc) of Packet First In First Out Head Drop (pfifo_head_drop)."""
      "PFIFO" -> """The [PFIFO] section manages the queueing discipline (qdisc) of Packet First In First Out (pfifo)."""
      "PIE" -> """The [PIE] section manages the queueing discipline (qdisc) of Proportional Integral Enhanced (PIE) for congestion control."""
      "QDisc" -> """The [QDisc] section configures generic queueing disciplines for traffic control."""
      "QuickFairQueueingClass" -> """The [QuickFairQueueingClass] section manages the traffic control class of Quick Fair Queueing (qfq)."""
      "QuickFairQueueing" -> """The [QuickFairQueueing] section manages the queueing discipline (qdisc) of Quick Fair Queueing (QFQ)."""
      "Route" -> """The [Route] section configures static routing entries for interfaces."""
      "RoutingPolicyRule" -> """The [RoutingPolicyRule] section configures rules for routing policy decisions."""
      "TokenBucketFilter" -> """The [TokenBucketFilter] section manages the queueing discipline (qdisc) of the token bucket filter (tbf)."""
      "TrivialLinkEqualizer" -> """The [TrivialLinkEqualizer] section manages the queueing discipline (qdisc) of Trivial Link Equalizer (teql)."""

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
  fun getDocumentationContentForKeyInSection(fileclass: FileClass, sectionName: String, keyName: String): String? {


    val htmlDocStream = this.javaClass.classLoader.getResourceAsStream(
      SEMANTIC_DATA_ROOT + "/documents/completion/" + fileclass.fileClass + "/" + sectionName + "/" + keyName + ".html"
    ) ?: return getDeprecationReason(fileclass, sectionName, keyName, true)
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
  fun getDeprecationReason(fileclass: FileClass, sectionName: String, keyName: String, html: Boolean): String? {
    val options = getKeyValuePairsForSectionFromUndocumentedInformation(fileclass, sectionName)[keyName] ?: return null
    return when (options.reason) {
      "unsupported" -> {
        if (!html) String.format(
          "'%s' in section '%s' is not officially supported",
          keyName,
          sectionName
        ) else ("<p><var>" + keyName + "</var> in section <b>" + sectionName + "</b> is not officially supported.<p>" + "<a href='" + options.documentationLink + "'>More information is available here</a>")
      }

      "moved" -> {
        val newKeyName = StringUtil.notNullize(options.replacedWithKey, keyName)
        val newSectionName = StringUtil.notNullize(options.replacedWithSection, sectionName)
        val semanticsNote = if (!html) "" else ("<p>NOTE: The semantics of the new value may not match the existing value.<p>" + "<a href='" + options.documentationLink + "'>More information is available here</a>")
        if (newSectionName == sectionName) {
          LOG.assertTrue(newKeyName != keyName, String.format("Meaningless move/rename of %s.%s", sectionName, keyName))
          return if (!html) {
            String.format("'%s' in section '%s' has been renamed to '%s'", keyName, sectionName, newKeyName)
          } else ("<p>The key <var>" + keyName + "</var> in section <b>" + sectionName + "</b> has been renamed to " + "<var>" + newKeyName + "</var>" + semanticsNote)
        }
        if (newKeyName == keyName) {
          return if (!html) {
            String.format("'%s' has been moved to section '%s'", keyName, newSectionName)
          } else ("<p>The key <var>" + keyName + "</var> in section <b>" + sectionName + "</b> has been moved to " + "section <b>" + newSectionName + "</b>" + semanticsNote)
        }
        if (!html) {
          String.format("'%s' in section '%s' has been moved to '%s' in section '%s'", keyName, sectionName, newKeyName, newSectionName)
        } else ("<p>The key <var>" + keyName + "</var> in section <b>" + sectionName + "</b> has been moved to " + "<var>" + newKeyName + "</var> in section <b>" + newSectionName + "</b>" + semanticsNote)
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
  fun isDeprecated(fileclass: FileClass, sectionName: String, keyName: String): Boolean {
    val options = getKeyValuePairsForSectionFromUndocumentedInformation(fileclass, sectionName)[keyName]
    return options != null
  }

  /**
   * Gets the validator for a section name and key name.
   *
   * @param sectionName the name of the section we are looking up
   * @param keyName - the keyname to look up
   * @return the validator
   */
  fun getOptionValidator(fileclass: FileClass, sectionName: String, keyName: String): OptionValueInformation {
    return validatorMap.get(getValidatorForSectionAndKey(fileclass, sectionName, keyName)) ?: NullOptionValue.INSTANCE
  }


  fun getValidatorMap(): Map<Validator, OptionValueInformation> = validatorMap


  /**
   * Return the section names from the validators.
   *
   * @return set
   */
  fun getSectionNamesForSectionAndKey(fileClass: FileClass): MutableSet<String> {
    return Collections.unmodifiableSet(fileClassToSectionToKeyAndValidatorMap.getOrDefault(fileClass.fileClass, emptyMap()).keys)
  }


  fun getValidatorForSectionAndKey(fileClass: FileClass, sectionName: String, keyName: String): Validator {

    var validatorKey = fileClassToSectionToKeyAndValidatorMap.getOrDefault(fileClass.fileClass, emptyMap()).getOrDefault(sectionName, emptyMap())[keyName]
    if (sectionName.trim { it <= ' ' } == "Install") {
      if (keyName.trim { it <= ' ' } == "WantedBy" || keyName.trim { it <= ' ' } == "Also" || keyName.trim { it <= ' ' } == "RequiredBy") { // Override the validators for these cases since there is no validation done now, but really they should be units.
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
  fun getAllowedKeywordsInSectionFromValidators(fileClass: FileClass, sectionName: String?): Set<String> {
    return Collections.unmodifiableSet(fileClassToSectionToKeyAndValidatorMap.getOrDefault(fileClass.fileClass, emptyMap()).getOrDefault(sectionName, emptyMap()).keys)
  }

  fun getAllowedSectionsInFile(fileName: String): Set<String> {

    val completeSections = when (getUnitType(fileName)) {
      "Automount" -> setOf("Unit", "Install", "Automount")
      "Device", "Target" -> setOf("Unit", "Install")
      "Mount" -> setOf("Unit", "Install", "Mount")
      "Path" -> setOf("Unit", "Install", "Path")
      "Service" -> setOf("Unit", "Install", "Service")
      "Slice" -> setOf("Unit", "Install", "Slice")
      "Socket" -> setOf("Unit", "Install", "Socket")
      "Swap" -> setOf("Unit", "Install", "Swap")
      "Timer" -> setOf("Unit", "Install", "Timer")
      "Nspawn" -> setOf("Exec", "Files", "Network")
      "Network" -> setOf(
        "Address", "BandMultiQueueing", "BFIFO", "Bridge", "BridgeFDB", "BridgeMDB", "BridgeVLAN", "CAKE", "CAN", "ClassfulMultiQueueing", "ControlledDelay", "DeficitRoundRobinScheduler", "DeficitRoundRobinSchedulerClass", "DHCPPrefixDelegation", "DHCPServer",
        "DHCPServerStaticLease", "DHCPv4", "DHCPv6", "EnhancedTransmissionSelection", "FairQueueing", "FairQueueingControlledDelay", "FlowQueuePIE", "GenericRandomEarlyDetection", "HeavyHitterFilter", "HierarchyTokenBucket", "HierarchyTokenBucketClass",
        "IPoIB", "IPv6AcceptRA", "IPv6AddressLabel", "IPv6PREF64Prefix", "IPv6Prefix", "IPv6RoutePrefix", "IPv6SendRA", "Link", "LLDP", "Match", "Neighbor",
        "Network", "NetworkEmulator", "NextHop", "PFIFO", "PFIFOFast", "PFIFOHeadDrop", "PIE", "QDisc", "QuickFairQueueing",
        "QuickFairQueueingClass", "Route", "RoutingPolicyRule", "StochasticFairBlue", "StochasticFairnessQueueing", "TokenBucketFilter", "TrivialLinkEqualizer",
      )
      "Link" -> setOf(
        "Link", "Match", "SR-IOV",
      )
      "Netdev" -> setOf(
        "BareUDP", "BatmanAdvanced", "Bond", "Bridge", "FooOverUDP", "GENEVE", "HSR", "IPoIB", "IPVLAN", "IPVTAP",
        "L2TP", "L2TPSession", "MACsec", "MACsecReceiveAssociation", "MACsecReceiveChannel", "MACsecTransmitAssociation", "MACVLAN",
        "MACVTAP", "Match", "NetDev", "Peer", "Tap", "Tun", "Tunnel", "VLAN",
        "VRF", "VXCAN", "VXLAN", "WireGuard", "WireGuardPeer", "WLAN", "Xfrm", "NetDev"
      )
      else -> setOf()
    }

    return completeSections
  }

  fun getUnitType(fileName: String): String {
    return fileName.substringAfterLast(".", "").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
  }

  fun getRequiredKeys(containingFile: String): Set<String> {
    val unitType = getUnitType(containingFile)

    return when (unitType) {
      "Service" -> setOf("Unit.SuccessAction", "Service.ExecStart", "Service.ExecStop")
      "Path" -> setOf("Path.PathExists", "Path.PathExistsGlob", "Path.PathChanged", "Path.PathModified", "Path.DirectoryNotEmpty")
      "Timer" -> setOf("Timer.OnActiveSec", "Timer.OnBootSec", "Timer.OnStartupSec", "Timer.OnUnitActiveSec", "Timer.OnUnitInactiveSec", "Timer.OnCalendar", "Timer.OnClockChange", "Timer.OnTimezoneChange")
      "Swap" -> setOf("Swap.What")
      "Mount" -> // Both of these are required, but we only error out on one :(
        // systemd-analyze only complains about what though, so ... we can ignore it until a bug comes.
        setOf("Mount.What", "Mount.Where")

      "Socket" -> setOf("Socket.ListenStream", "Socket.ListenDatagram", "Socket.ListenSequentialPacket", "Socket.ListenFIFO", "Socket.ListenSpecial", "Socket.ListenNetlink", "Socket.ListenMessageQueue", "Socket.ListenUSBFunction")
      "Automount" -> setOf("Automount.Where") // NSpawn?
      // Network
      // Link
      // NetDev
      else -> setOf()
    }

  }

  companion object {
    private val LOG = Logger.getInstance(SemanticDataRepository::class.java)
    const val SEMANTIC_DATA_ROOT = "net/sjrx/intellij/plugins/systemdunitfiles/semanticdata/"
    private const val GPERF_REGEX = """^(?<Section>[a-zA-Z0-9_-]+)\.(?<Key>[a-zA-Z0-9_-]+)\s*,\s*(?<Validator>\w+)\s*,\s*(?<MysteryColumn1>\w+)\s*,\s*(?<MysteryColumn2>.+)$"""
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

    fun getFileClassForFilename(fileName: String): FileClass {
      return when (File(fileName).extension) {
        "nspawn" -> FileClass.NSPAWN
        "service", "socket", "target", "device", "mount", "automount", "path", "swap", "timer", "slice" -> FileClass.UNIT_FILE
        "link" -> FileClass.LINK
        "netdev" -> FileClass.NETDEV
        "network" -> FileClass.NETWORK
        else -> FileClass.UNIT_FILE
      }
    }
  }
}

data class Validator(val validatorName: String, val validatorArgument: String = "0") {
  override fun toString() = "${validatorName}(${validatorArgument})"
}
