package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.Strings;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import net.sjrx.intellij.plugins.systemdunitfiles.psi.UnitFilePropertyType;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.SemanticDataRepository.SEMANTIC_DATA_ROOT;

/**
 * This validator is used for the config_parse_unit_deps validator.
 */
public class UnitDependencyOptionValue implements OptionValueInformation {
  
  private static volatile Set<String> unitNames = null;
  
  private static final Set<String> validUnitTypes =
    Set.of("service", "socket", "device", "mount", "automount", "swap", "target", "path", "timer", "slice", "scope");
  
  /**
   * Default Constructor.
   */
  public UnitDependencyOptionValue() {
    if (unitNames == null) {
      String filename = SEMANTIC_DATA_ROOT + "ubuntu-units.txt";
      URL unitListing =
        this.getClass().getClassLoader().getResource(filename);
      
      try (BufferedReader in = new BufferedReader(new InputStreamReader(unitListing.openStream()))) {
        String inputLine;
        SortedSet<String> unitNames = new TreeSet<>();
        while ((inputLine = in.readLine()) != null) {
          unitNames.add(inputLine);
        }
        UnitDependencyOptionValue.unitNames = Collections.unmodifiableSet(unitNames);
      } catch (NullPointerException | IOException e) {
        throw new IllegalStateException("Could not read file: " + filename, e);
      }
    }
  }
  
  @Override
  public Set<String> getAutoCompleteOptions(@NotNull Project project) {
    Set<String> autoCompletionOptions = new HashSet<>(unitNames);
    
    for (String unitTypes: validUnitTypes) {
      for (VirtualFile file : FilenameIndex.getAllFilesByExt(project, unitTypes)) {
        autoCompletionOptions.add(file.getName());
        
      }
    }

    return autoCompletionOptions;
  }
  
  @Override
  public String getErrorMessage(String value) {
    throw new IllegalStateException("Not supported");
  }
  
  @Override
  public void generateProblemDescriptors(@NotNull UnitFilePropertyType property, @NotNull ProblemsHolder holder) {
    // Valid unit names consist of a "name prefix" and a dot and a suffix specifying the unit type.
    // The "unit prefix" must consist of one or more valid characters (ASCII letters, digits, ":", "-", "_", ".", and "\").
    // The total length of the unit name including the suffix must not exceed 256 characters.
    // The type suffix must be one of ".service", ".socket", ".device", ".mount", ".automount", ".swap", ".target", ".path", ".timer", ".slice", or ".scope".
    
    String values = property.getValueNode().getText();
    
    int processedCharactersInString = 0;
    while (processedCharactersInString < values.length()) {
      int indexOfNextSpace = values.indexOf(" ", processedCharactersInString);
      
      int endOfWord = values.length();
      if (indexOfNextSpace > -1) {
        endOfWord = indexOfNextSpace;
      }
      
      String word = values.substring(processedCharactersInString, endOfWord);
      
      if (word.length() == 0) {
        processedCharactersInString += 1;
        continue;
      }
      
      int lastDot = word.lastIndexOf('.');
      
      TextRange range = new TextRange(processedCharactersInString + lastDot + 1, endOfWord);
      if (lastDot > -1) {
        // Valid unit name
        
        String unitType = word.substring(lastDot + 1);

        if (!(validUnitTypes.contains(unitType))) {
          holder.registerProblem(property.getValueNode().getPsi(), range,
            "Unit type " + unitType + " is unsupported, valid types are: " + Strings.join(validUnitTypes, ", "));
        }
        
      } else {
        holder.registerProblem(property.getValueNode().getPsi(), range, "Invalid unit name detected, all units must end with a . and then a unit type suffix");
      }
      
      processedCharactersInString += word.length();
    }
    return;
  }
  
  @Override
  public String getValidatorName() {
    return "config_parse_unit_deps";
  }
}
