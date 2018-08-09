// This is a generated file. Not intended for manual editing.
package net.sjrx.intellij.plugins.systemdunitfiles.generated;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static net.sjrx.intellij.plugins.systemdunitfiles.generated.UnitFileElementTypeHolder.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class UnitFileParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, null);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    if (root_ == PROPERTY) {
      result_ = property(builder_, 0);
    }
    else if (root_ == SECTION_GROUPS) {
      result_ = section_groups(builder_, 0);
    }
    else {
      result_ = parse_root_(root_, builder_, 0);
    }
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType root_, PsiBuilder builder_, int level_) {
    return unitFile(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // property|COMMENT|CRLF
  static boolean line_items_(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "line_items_")) return false;
    boolean result_;
    result_ = property(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, COMMENT);
    if (!result_) result_ = consumeToken(builder_, CRLF);
    return result_;
  }

  /* ********************************************************** */
  // KEY SEPARATOR VALUE
  public static boolean property(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property")) return false;
    if (!nextTokenIs(builder_, KEY)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, KEY, SEPARATOR, VALUE);
    exit_section_(builder_, marker_, PROPERTY, result_);
    return result_;
  }

  /* ********************************************************** */
  // SECTION line_items_*
  public static boolean section_groups(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "section_groups")) return false;
    if (!nextTokenIs(builder_, SECTION)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SECTION);
    result_ = result_ && section_groups_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, SECTION_GROUPS, result_);
    return result_;
  }

  // line_items_*
  private static boolean section_groups_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "section_groups_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!line_items_(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "section_groups_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // (COMMENT|CRLF)* section_groups*
  static boolean unitFile(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unitFile")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = unitFile_0(builder_, level_ + 1);
    result_ = result_ && unitFile_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMENT|CRLF)*
  private static boolean unitFile_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unitFile_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!unitFile_0_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "unitFile_0", pos_)) break;
    }
    return true;
  }

  // COMMENT|CRLF
  private static boolean unitFile_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unitFile_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, COMMENT);
    if (!result_) result_ = consumeToken(builder_, CRLF);
    return result_;
  }

  // section_groups*
  private static boolean unitFile_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unitFile_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!section_groups(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "unitFile_1", pos_)) break;
    }
    return true;
  }

}
