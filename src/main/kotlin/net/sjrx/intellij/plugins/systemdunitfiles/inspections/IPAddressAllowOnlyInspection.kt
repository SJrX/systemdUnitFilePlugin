package net.sjrx.intellij.plugins.systemdunitfiles.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import net.sjrx.intellij.plugins.systemdunitfiles.intentions.AddPropertyAndValueQuickFix
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileSectionGroups
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFileVisitor
import net.sjrx.intellij.plugins.systemdunitfiles.intentions.AddPropertyQuickFix
import java.util.*

/**
 * This inspection warns when IPAddressAllow is specified without IPAddressDeny in certain sections,
 * as this configuration does not block traffic (the default action is to permit).
 */
class IPAddressAllowOnlyInspection : LocalInspectionTool() {

    // Sections where this inspection applies
    private val targetSections = setOf("Slice", "Scope", "Service", "Socket")

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : UnitFileVisitor() {
            // Map to track properties by section
            private val sectionProperties = mutableMapOf<String, MutableSet<String>>()

            override fun visitFile(file: PsiFile) {
                super.visitFile(file)

                // After visiting the file, check each section for the condition
                for ((section, properties) in sectionProperties) {
                    if (section in targetSections &&
                        "IPAddressAllow" in properties &&
                        "IPAddressDeny" !in properties) {

                        // Find all IPAddressAllow properties in this section to highlight
                        val sectionElement = findSectionElement(file, section)
                        if (sectionElement != null) {
                            val ipAddressAllowProperties = findPropertiesInSection(sectionElement, "IPAddressAllow")

                            for (property in ipAddressAllowProperties) {
                                holder.registerProblem(
                                    property,
                                    "Specifying IPAddressAllow without IPAddressDeny does not block traffic as the default action is to permit",
                                    com.intellij.codeInspection.ProblemHighlightType.WEAK_WARNING,
                                    AddPropertyAndValueQuickFix(section, "IPAddressDeny", "any")
                                )
                            }
                        }
                    }
                }
            }

            override fun visitPropertyType(property: UnitFilePropertyType) {
                super.visitPropertyType(property)

                val section = PsiTreeUtil.getParentOfType(property, UnitFileSectionGroups::class.java)
                if (section != null && section.sectionName in targetSections) {
                    // Add this property to the section's property set
                    val sectionName = section.sectionName
                    if (!sectionProperties.containsKey(sectionName)) {
                        sectionProperties[sectionName] = mutableSetOf()
                    }
                    sectionProperties[sectionName]?.add(property.key)
                }
            }

            /**
             * Find a section element by name in the file
             */
            private fun findSectionElement(file: PsiFile, sectionName: String): UnitFileSectionGroups? {
                val sections = PsiTreeUtil.findChildrenOfType(file, UnitFileSectionGroups::class.java)
                return sections.find { it.sectionName == sectionName }
            }

            /**
             * Find all properties with a specific key in a section
             */
            private fun findPropertiesInSection(section: UnitFileSectionGroups, propertyKey: String): List<UnitFilePropertyType> {
                val properties = PsiTreeUtil.findChildrenOfType(section, UnitFilePropertyType::class.java)
                return properties.filter { it.key == propertyKey }
            }
        }
    }
}
