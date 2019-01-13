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
    else if (root_ == VALUE) {
      result_ = value(builder_, 0);
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
  // COMMENT
  static boolean comment_(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "comment_")) return false;
    if (!nextTokenIs(builder_, "<comment>", COMMENT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null, "<comment>");
    result_ = consumeToken(builder_, COMMENT);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // CRLF
  static boolean crlf_(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "crlf_")) return false;
    if (!nextTokenIs(builder_, "<new line>", CRLF)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null, "<new line>");
    result_ = consumeToken(builder_, CRLF);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // KEY
  static boolean key_(PsiBuilder builder_, int level_) {
    return consumeToken(builder_, KEY);
  }

  /* ********************************************************** */
  // (property crlf_?)|(comment_ crlf_?)|crlf_
  static boolean line_items_(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "line_items_")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = line_items__0(builder_, level_ + 1);
    if (!result_) result_ = line_items__1(builder_, level_ + 1);
    if (!result_) result_ = crlf_(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // property crlf_?
  private static boolean line_items__0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "line_items__0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = property(builder_, level_ + 1);
    result_ = result_ && line_items__0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // crlf_?
  private static boolean line_items__0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "line_items__0_1")) return false;
    crlf_(builder_, level_ + 1);
    return true;
  }

  // comment_ crlf_?
  private static boolean line_items__1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "line_items__1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = comment_(builder_, level_ + 1);
    result_ = result_ && line_items__1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // crlf_?
  private static boolean line_items__1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "line_items__1_1")) return false;
    crlf_(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // key_ separator_ value?
  public static boolean property(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property")) return false;
    if (!nextTokenIs(builder_, KEY)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PROPERTY, null);
    result_ = key_(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, separator_(builder_, level_ + 1));
    result_ = pinned_ && property_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // value?
  private static boolean property_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property_2")) return false;
    value(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // SECTION
  static boolean section_(PsiBuilder builder_, int level_) {
    return consumeToken(builder_, SECTION);
  }

  /* ********************************************************** */
  // section_ line_items_*
  public static boolean section_groups(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "section_groups")) return false;
    if (!nextTokenIs(builder_, "<section header>", SECTION)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SECTION_GROUPS, "<section header>");
    result_ = section_(builder_, level_ + 1);
    result_ = result_ && section_groups_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
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
  // SEPARATOR
  static boolean separator_(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "separator_")) return false;
    if (!nextTokenIs(builder_, "<key-value separator (=)>", SEPARATOR)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null, "<key-value separator (=)>");
    result_ = consumeToken(builder_, SEPARATOR);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // (comment_|crlf_)* section_groups*
  static boolean unitFile(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unitFile")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = unitFile_0(builder_, level_ + 1);
    result_ = result_ && unitFile_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (comment_|crlf_)*
  private static boolean unitFile_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unitFile_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!unitFile_0_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "unitFile_0", pos_)) break;
    }
    return true;
  }

  // comment_|crlf_
  private static boolean unitFile_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unitFile_0_0")) return false;
    boolean result_;
    result_ = comment_(builder_, level_ + 1);
    if (!result_) result_ = crlf_(builder_, level_ + 1);
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

  /* ********************************************************** */
  // (CONTINUING_VALUE| comment_ )* COMPLETED_VALUE
  public static boolean value(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "value")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, VALUE, "<value>");
    result_ = value_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMPLETED_VALUE);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (CONTINUING_VALUE| comment_ )*
  private static boolean value_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "value_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!value_0_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "value_0", pos_)) break;
    }
    return true;
  }

  // CONTINUING_VALUE| comment_
  private static boolean value_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "value_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, CONTINUING_VALUE);
    if (!result_) result_ = comment_(builder_, level_ + 1);
    return result_;
  }

}
