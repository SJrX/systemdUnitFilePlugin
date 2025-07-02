package net.sjrx.intellij.plugins.systemdunitfiles.semanticdata.optionvalues.grammar

import junit.framework.TestCase

class GrammarTest : TestCase() {

  fun TerminalType(o: TerminalCombinator): String {
    return o.javaClass.simpleName
  }

  fun TerminalTypes(os: List<TerminalCombinator>): List<String> {
    return os.map { o -> TerminalType(o) }
  }

  fun testRegexTerminalMatches() {
    /**
     * Fixture Setup
     */

    // This regex should match something that looks like a byte specifier possibly negative, syntactically.
    // But semantically only accept positive values and known units.

    val regexTerminal = RegexTerminal("-?[0-9]+\\s*[A-Z]", "[0-9]*[1-9]\\s*[BKMG]")

    val semValid = "1K"
    val synValid = "-2Z"
    val invalid = "6,000 People"

    val garbage = "XX"
    val semValidFromOffset = "${garbage}${semValid}"
    val synValidFromOffset = "${garbage}${synValid}"
    val invalidFromOffset = "${garbage}${invalid}"


    /**
     * Execute SUT & Verification
     */
    var match = regexTerminal.SemanticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("1K"), match.tokens)
    assertEquals(listOf("RegexTerminal"), TerminalTypes(match.terminals))

    match = regexTerminal.SyntacticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("1K"), match.tokens)
    assertEquals(listOf("RegexTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, regexTerminal.SemanticMatch(synValid, 0))

    match = regexTerminal.SyntacticMatch(synValid, 0)
    assertEquals(synValid.length, match.matchResult)
    assertEquals(listOf("-2Z"), match.tokens)
    assertEquals(listOf("RegexTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, regexTerminal.SemanticMatch(invalid, 0))
    assertEquals(NoMatch, regexTerminal.SyntacticMatch(invalid, 0))

    match = regexTerminal.SemanticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("1K"), match.tokens)
    assertEquals(listOf("RegexTerminal"), TerminalTypes(match.terminals))

    match = regexTerminal.SyntacticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("1K"), match.tokens)
    assertEquals(listOf("RegexTerminal"), TerminalTypes(match.terminals))

    match = regexTerminal.SyntacticMatch(synValidFromOffset, garbage.length)
    assertEquals(synValidFromOffset.length, match.matchResult)
    assertEquals(listOf("-2Z"), match.tokens)
    assertEquals(listOf("RegexTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, regexTerminal.SemanticMatch(synValidFromOffset, garbage.length))
    assertEquals(NoMatch, regexTerminal.SyntacticMatch(invalidFromOffset, garbage.length))
    assertEquals(NoMatch, regexTerminal.SemanticMatch(invalidFromOffset, garbage.length))


  }


  fun testLiteralChoiceTerminalMatches() {
    /**
     * Fixture Setup
     */

    // This combinator should match the options passed in.

    val literalChoiceTerminal = LiteralChoiceTerminal( "foo", "bar", "baz")

    val semValid = "foo"
    val garbage = "XX"
    val invalid = "qux"

    val semValidFromOffset="${garbage}${semValid}"
    val invalidFromOffset = "${garbage}${invalid}"
    val invalidFromOffsetWithSemValidPrefix = "${semValid}${invalid}"

    /**
     * Execute SUT & Verification
     */
    var match = literalChoiceTerminal.SemanticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("foo"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = literalChoiceTerminal.SyntacticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("foo"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, literalChoiceTerminal.SemanticMatch(invalid, 0))
    assertEquals(NoMatch, literalChoiceTerminal.SyntacticMatch(invalid, 0))

    match = literalChoiceTerminal.SemanticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("foo"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = literalChoiceTerminal.SyntacticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("foo"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, literalChoiceTerminal.SemanticMatch(invalidFromOffset, garbage.length))
    assertEquals(NoMatch, literalChoiceTerminal.SyntacticMatch(invalidFromOffset, garbage.length))

    assertEquals(NoMatch, literalChoiceTerminal.SemanticMatch(invalidFromOffsetWithSemValidPrefix, semValid.length))
    assertEquals(NoMatch, literalChoiceTerminal.SyntacticMatch(invalidFromOffsetWithSemValidPrefix, semValid.length))

  }

  fun testLiteralChoiceTerminalMatchesLongest() {
    /**
     * Fixture Setup
     */

    // This combinator should match the options passed in.

    val literalChoiceTerminal = LiteralChoiceTerminal( "a", "ab", "abc")

    val semValid = "abc"
    val garbage = "XX"

    val semValidFromOffset="${garbage}${semValid}"

    /**
     * Execute SUT & Verfication
     */
    var match = literalChoiceTerminal.SemanticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("abc"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = literalChoiceTerminal.SyntacticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("abc"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))
  }

  fun testFlexibleLiteralChoiceTerminalMatches() {
    /**
     * Fixture Setup
     */

    // This combinator should match the options passed in.

    val literalChoiceTerminal = FlexibleLiteralChoiceTerminal( "foo", "bar", "baz")

    val semValid = "foo"
    val garbage = "XX"
    val invalid = "qux"

    val semValidFromOffset="${garbage}${semValid}"
    val invalidFromOffset = "${garbage}${invalid}"
    val invalidFromOffsetWithSemValidPrefix = "${semValid}${invalid}"

    /**
     * Execute SUT & Verification
     */
    var match = literalChoiceTerminal.SemanticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("foo"), match.tokens)
    assertEquals(listOf("FlexibleLiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = literalChoiceTerminal.SyntacticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("foo"), match.tokens)
    assertEquals(listOf("FlexibleLiteralChoiceTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, literalChoiceTerminal.SemanticMatch(invalid, 0))

    match = literalChoiceTerminal.SyntacticMatch(invalid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("qux"), match.tokens)
    assertEquals(listOf("FlexibleLiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = literalChoiceTerminal.SemanticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("foo"), match.tokens)
    assertEquals(listOf("FlexibleLiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = literalChoiceTerminal.SyntacticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("foo"), match.tokens)
    assertEquals(listOf("FlexibleLiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = literalChoiceTerminal.SemanticMatch(invalidFromOffset, garbage.length)
    assertEquals(-1, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)
    assertEquals(listOf<String>(), TerminalTypes(match.terminals))
    assertEquals(2, match.longestMatch)

    match = literalChoiceTerminal.SyntacticMatch(invalidFromOffset, garbage.length)
    assertEquals(5, match.matchResult)
    assertEquals(listOf<String>("qux"), match.tokens)
    assertEquals(listOf<String>("FlexibleLiteralChoiceTerminal"), TerminalTypes(match.terminals))
    assertEquals(5, match.longestMatch)



    match = literalChoiceTerminal.SemanticMatch(invalidFromOffsetWithSemValidPrefix, semValid.length)
    assertEquals(-1, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)
    assertEquals(listOf<String>(), TerminalTypes(match.terminals))
    assertEquals(3, match.longestMatch)

    match = literalChoiceTerminal.SyntacticMatch(invalidFromOffsetWithSemValidPrefix, semValid.length)
    assertEquals(6, match.matchResult)
    assertEquals(listOf<String>("qux"), match.tokens)
    assertEquals(listOf<String>("FlexibleLiteralChoiceTerminal"), TerminalTypes(match.terminals))
    assertEquals(6, match.longestMatch)

  }

  fun testFlexibleLiteralChoiceTerminalMatchesLongest() {
    /**
     * Fixture Setup
     */

    // This combinator should match the options passed in.

    val literalChoiceTerminal = FlexibleLiteralChoiceTerminal( "a", "ab", "abc")

    val semValid = "abc"
    val garbage = "XX"

    val semValidFromOffset="${garbage}${semValid}"

    /**
     * Execute SUT & Verfication
     */
    var match = literalChoiceTerminal.SemanticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("abc"), match.tokens)
    assertEquals(listOf("FlexibleLiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = literalChoiceTerminal.SyntacticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("abc"), match.tokens)
    assertEquals(listOf("FlexibleLiteralChoiceTerminal"), TerminalTypes(match.terminals))
  }

  fun testFlexibleLiteralChoiceTerminalMatchesSemanticallyFirstLongest() {
    /**
     * Fixture Setup
     */

    // This combinator should match the options passed in.

    val literalChoiceTerminal = FlexibleLiteralChoiceTerminal( "abc", "defg")

    val semValid = "abc"
    val garbage = "xx"

    val semValidAndGarbage = "${semValid}${garbage}"


    /**
     * Execute SUT & Verfication
     */
    var match = literalChoiceTerminal.SemanticMatch(semValidAndGarbage, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("abc"), match.tokens)
    assertEquals(listOf("FlexibleLiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = literalChoiceTerminal.SyntacticMatch(semValidAndGarbage, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("abc"), match.tokens)
    assertEquals(listOf("FlexibleLiteralChoiceTerminal"), TerminalTypes(match.terminals))
  }




  fun testSequenceCombinatorMatches() {
    /**
     * Fixture Setup
     */

    // This combinator should match the options passed in.
    val number = RegexTerminal("-?[0-9]+", "[0-9]*[1-9]")
    val unit = LiteralChoiceTerminal("B", "K", "M", "G")
    val sequenceCombinator = SequenceCombinator(number, unit)

    val semValid = "1K"
    val synValid = "-0B"
    val semPrefix = "1"
    val synPrefix = "0"
    val invalid = "Hello World"

    val garbage = "XX"

    val semValidFromOffset = "${garbage}${semValid}"
    val synValidFromOffset = "${garbage}${synValid}"

    val semPrefixFromOffset = "${garbage}${semPrefix}"
    val synPrefixFromOffset = "${garbage}${synPrefix}"
    val invalidFromOffset = "${garbage}${invalid}"

    /**
     * Execute SUT & Verification
     */
    var match = sequenceCombinator.SyntacticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("1", "K"), match.tokens)
    assertEquals(listOf("RegexTerminal","LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = sequenceCombinator.SemanticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("1", "K"), match.tokens)
    assertEquals(listOf("RegexTerminal","LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = sequenceCombinator.SyntacticMatch(synValid, 0)
    assertEquals(synValid.length, match.matchResult)
    assertEquals(listOf("-0", "B"), match.tokens)
    assertEquals(listOf("RegexTerminal","LiteralChoiceTerminal"), TerminalTypes(match.terminals))


    assertEquals(NoMatch, sequenceCombinator.SemanticMatch(synValid, 0))

    match = sequenceCombinator.SyntacticMatch(semPrefix, 0)
    assertEquals(-1, match.matchResult)

    match = sequenceCombinator.SemanticMatch(semPrefix, 0)
    assertEquals(-1, match.matchResult)

    match = sequenceCombinator.SyntacticMatch(synPrefix, 0)
    assertEquals(-1, match.matchResult)

    match = sequenceCombinator.SemanticMatch(synPrefix, 0)
    assertEquals(-1, match.matchResult)

    match = sequenceCombinator.SyntacticMatch(invalid, 0)
    assertEquals(-1, match.matchResult)

    match = sequenceCombinator.SemanticMatch(invalid, 0)
    assertEquals(-1, match.matchResult)

    match = sequenceCombinator.SemanticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("1", "K"), match.tokens)
    assertEquals(listOf("RegexTerminal","LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = sequenceCombinator.SyntacticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("1", "K"), match.tokens)
    assertEquals(listOf("RegexTerminal","LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = sequenceCombinator.SyntacticMatch(synValidFromOffset, garbage.length)
    assertEquals(synValidFromOffset.length, match.matchResult)
    assertEquals(listOf("-0", "B"), match.tokens)
    assertEquals(listOf("RegexTerminal","LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, sequenceCombinator.SemanticMatch(synValidFromOffset, garbage.length))
    match = sequenceCombinator.SemanticMatch(semPrefixFromOffset, garbage.length)
    assertEquals(-1, match.matchResult)

    match = sequenceCombinator.SyntacticMatch(semPrefixFromOffset, garbage.length)
    assertEquals(-1, match.matchResult)
    assertEquals(NoMatch, sequenceCombinator.SemanticMatch(synPrefixFromOffset, garbage.length))

    match = sequenceCombinator.SyntacticMatch(synPrefixFromOffset, garbage.length)
    assertEquals(-1, match.matchResult)

    assertEquals(NoMatch, sequenceCombinator.SemanticMatch(invalidFromOffset, garbage.length))
    assertEquals(NoMatch, sequenceCombinator.SyntacticMatch(invalidFromOffset, garbage.length))
  }


  fun testAlternativeCombinatorMatches() {
    /**
     * Fixture Setup
     */

    // This combinator should match the options passed in.

    val on = LiteralChoiceTerminal("on")
    val off = LiteralChoiceTerminal("off")

    val alternativeCombinator = AlternativeCombinator(on, off)

    val semValid = "on"
    val semValid2 = "off"
    val invalid = "bleh"

    val garbage = "XX"

    val semValidFromOffset = "${garbage}${semValid}"
    val semValid2FromOffset = "${garbage}${semValid2}"
    val invalidFromOffset = "${garbage}${invalid}"

    /**
     * Execute SUT & Verification
     */
    var match = alternativeCombinator.SyntacticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("on"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = alternativeCombinator.SemanticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("on"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = alternativeCombinator.SyntacticMatch(semValid2, 0)
    assertEquals(semValid2.length, match.matchResult)
    assertEquals(listOf("off"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = alternativeCombinator.SemanticMatch(semValid2, 0)
    assertEquals(semValid2.length, match.matchResult)
    assertEquals(listOf("off"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, alternativeCombinator.SyntacticMatch(invalid, 0))
    assertEquals(NoMatch, alternativeCombinator.SemanticMatch(invalid, 0))

    match = alternativeCombinator.SemanticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("on"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = alternativeCombinator.SyntacticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("on"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = alternativeCombinator.SemanticMatch(semValid2FromOffset, garbage.length)
    assertEquals(semValid2FromOffset.length, match.matchResult)
    assertEquals(listOf("off"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = alternativeCombinator.SyntacticMatch(semValid2FromOffset, garbage.length)
    assertEquals(semValid2FromOffset.length, match.matchResult)
    assertEquals(listOf("off"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, alternativeCombinator.SemanticMatch(invalidFromOffset, garbage.length))
    assertEquals(NoMatch, alternativeCombinator.SyntacticMatch(invalidFromOffset, garbage.length))
  }

  fun testOneOrMoreCombinatorMatches() {
    /**
     * Fixture Setup
     */

    // This combinator should match the options passed in.
    val fizzOrBuzz = RegexTerminal("[a-z]{4}", "fizz|buzz")

    val oneOrMoreCombinator = OneOrMore(fizzOrBuzz)

    val semValid = "fizzbuzzfizz"
    val synValid = "blehblehbleh"
    val invalid = "Hello World"
    val garbage = "XX"

    val semValidFromOffset = "${garbage}${semValid}"
    val synValidFromOffset = "${garbage}${synValid}"
    val invalidFromOffset = "${garbage}${invalid}"

    /**
     * Execute SUT & Verification
     */
    var match = oneOrMoreCombinator.SyntacticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("fizz", "buzz", "fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = oneOrMoreCombinator.SemanticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("fizz", "buzz", "fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = oneOrMoreCombinator.SyntacticMatch(synValid, 0)
    assertEquals(synValid.length, match.matchResult)
    assertEquals(listOf("bleh", "bleh", "bleh"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, oneOrMoreCombinator.SemanticMatch(synValid, 0))

    assertEquals(NoMatch, oneOrMoreCombinator.SyntacticMatch(invalid, 0))
    assertEquals(NoMatch, oneOrMoreCombinator.SemanticMatch(invalid, 0))

    match = oneOrMoreCombinator.SyntacticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("fizz", "buzz", "fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = oneOrMoreCombinator.SemanticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("fizz", "buzz", "fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = oneOrMoreCombinator.SyntacticMatch(synValidFromOffset, garbage.length)
    assertEquals(synValidFromOffset.length, match.matchResult)
    assertEquals(listOf("bleh", "bleh", "bleh"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, oneOrMoreCombinator.SemanticMatch(synValidFromOffset, garbage.length))

    assertEquals(NoMatch, oneOrMoreCombinator.SyntacticMatch(invalidFromOffset, garbage.length))
    assertEquals(NoMatch, oneOrMoreCombinator.SemanticMatch(invalidFromOffset, garbage.length))

  }

  fun testZeroOrMoreCombinatorMatches() {
    /**
     * Fixture Setup
     */

    // This combinator should match the options passed in.
    val fizzOrBuzz = RegexTerminal("[a-z]{4}", "fizz|buzz")

    val zeroOrMoreCombinator = ZeroOrMore(fizzOrBuzz)

    val semValid = "fizzbuzzfizz"
    val synValid = "blehblehbleh"
    val semValidEmpty = ""
    val invalid = "Hello World"
    val garbage = "XX"

    val semValidFromOffset = "${garbage}${semValid}"
    val semValidEmptyFromOffset = "${garbage}${semValidEmpty}"
    val synValidFromOffset = "${garbage}${synValid}"
    val invalidFromOffset = "${garbage}${invalid}"

    /**
     * Execute SUT & Verification
     */
    var match = zeroOrMoreCombinator.SyntacticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("fizz", "buzz", "fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = zeroOrMoreCombinator.SemanticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("fizz", "buzz", "fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = zeroOrMoreCombinator.SyntacticMatch(synValid, 0)
    assertEquals(synValid.length, match.matchResult)
    assertEquals(listOf("bleh", "bleh", "bleh"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = zeroOrMoreCombinator.SemanticMatch(synValid, 0)
    assertEquals(0, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrMoreCombinator.SyntacticMatch(semValidEmpty, 0)
    assertEquals(semValidEmpty.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrMoreCombinator.SemanticMatch(semValidEmpty, 0)
    assertEquals(semValidEmpty.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrMoreCombinator.SyntacticMatch(invalid, 0)
    assertEquals(0, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrMoreCombinator.SemanticMatch(invalid, 0)
    assertEquals(0, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrMoreCombinator.SyntacticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("fizz", "buzz", "fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = zeroOrMoreCombinator.SemanticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("fizz", "buzz", "fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = zeroOrMoreCombinator.SyntacticMatch(synValidFromOffset, garbage.length)
    assertEquals(synValidFromOffset.length, match.matchResult)
    assertEquals(listOf("bleh", "bleh", "bleh"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = zeroOrMoreCombinator.SemanticMatch(synValidFromOffset, garbage.length)
    assertEquals(garbage.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrMoreCombinator.SyntacticMatch(semValidEmptyFromOffset, garbage.length)
    assertEquals(garbage.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrMoreCombinator.SemanticMatch(semValidEmptyFromOffset, garbage.length)
    assertEquals(garbage.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrMoreCombinator.SyntacticMatch(invalidFromOffset, garbage.length)
    assertEquals(garbage.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrMoreCombinator.SemanticMatch(invalidFromOffset, garbage.length)
    assertEquals(garbage.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)
  }

  fun testZeroOrOneCombinatorMatches() {
    /**
     * Fixture Setup
     */

    // This combinator should match the options passed in.
    val fizzOrBuzz = RegexTerminal("[a-z]{4}", "fizz|buzz")

    val zeroOrOneCombinator = ZeroOrOne(fizzOrBuzz)

    val semValid = "fizz"
    val synValid = "bleh"
    val semValidEmpty = ""
    val invalid = "Hello World"
    val garbage = "XX"

    val semValidFromOffset = "${garbage}${semValid}"
    val semValidEmptyFromOffset = "${garbage}${semValidEmpty}"
    val synValidFromOffset = "${garbage}${synValid}"
    val invalidFromOffset = "${garbage}${invalid}"

    /**
     * Execute SUT & Verification
     */
    var match = zeroOrOneCombinator.SyntacticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal"), TerminalTypes(match.terminals))

    match = zeroOrOneCombinator.SemanticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal"), TerminalTypes(match.terminals))

    match = zeroOrOneCombinator.SyntacticMatch(synValid, 0)
    assertEquals(synValid.length, match.matchResult)
    assertEquals(listOf("bleh"), match.tokens)
    assertEquals(listOf("RegexTerminal"), TerminalTypes(match.terminals))

    match = zeroOrOneCombinator.SemanticMatch(synValid, 0)
    assertEquals(0, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrOneCombinator.SyntacticMatch(semValidEmpty, 0)
    assertEquals(semValidEmpty.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrOneCombinator.SemanticMatch(semValidEmpty, 0)
    assertEquals(semValidEmpty.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrOneCombinator.SyntacticMatch(invalid, 0)
    assertEquals(0, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrOneCombinator.SemanticMatch(invalid, 0)
    assertEquals(0, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrOneCombinator.SyntacticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal"), TerminalTypes(match.terminals))

    match = zeroOrOneCombinator.SemanticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal"), TerminalTypes(match.terminals))

    match = zeroOrOneCombinator.SyntacticMatch(synValidFromOffset, garbage.length)
    assertEquals(synValidFromOffset.length, match.matchResult)
    assertEquals(listOf("bleh"), match.tokens)
    assertEquals(listOf("RegexTerminal"), TerminalTypes(match.terminals))

    match = zeroOrOneCombinator.SemanticMatch(synValidFromOffset, garbage.length)
    assertEquals(garbage.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrOneCombinator.SyntacticMatch(semValidEmptyFromOffset, garbage.length)
    assertEquals(garbage.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrOneCombinator.SemanticMatch(semValidEmptyFromOffset, garbage.length)
    assertEquals(garbage.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrOneCombinator.SyntacticMatch(invalidFromOffset, garbage.length)
    assertEquals(garbage.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = zeroOrOneCombinator.SemanticMatch(invalidFromOffset, garbage.length)
    assertEquals(garbage.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)
  }

  fun testEOFCombinatorMatches() {
    /**
     * Fixture Setup
     */

    // This combinator should match the options passed in.

    val eof = EOF()

    val semValid = ""
    val invalid = "Hello World"
    val garbage = "XX"

    val semValidFromOffset = "${garbage}${semValid}"
    val invalidFromOffset = "${garbage}${invalid}"

    /**
     * Execute SUT & Verification
     */
    var match = eof.SyntacticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = eof.SemanticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    assertEquals(NoMatch, eof.SyntacticMatch(invalid, 0))
    assertEquals(NoMatch, eof.SemanticMatch(invalid, 0))

    match = eof.SyntacticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    match = eof.SemanticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf<String>(), match.tokens)

    assertEquals(NoMatch, eof.SyntacticMatch(invalidFromOffset, garbage.length))
    assertEquals(NoMatch, eof.SemanticMatch(invalidFromOffset, garbage.length))
  }

  fun testOptionalWhitespacePrefixMatches() {
    /**
     * Fixture Setup
     */

    // This combinator should match the options passed in.

    val on = LiteralChoiceTerminal("on")


    val optionalWhitespacePrefix = OptionalWhitespacePrefix(on)

    val semValid = "on"
    val semValid2 = "\t on"
    val invalid = "off"

    val garbage = "XX"

    val semValidFromOffset = "${garbage}${semValid}"
    val semValid2FromOffset = "${garbage}${semValid2}"
    val invalidFromOffset = "${garbage}${invalid}"

    /**
     * Execute SUT & Verification
     */
    var match = optionalWhitespacePrefix.SyntacticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("on"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = optionalWhitespacePrefix.SemanticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("on"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = optionalWhitespacePrefix.SyntacticMatch(semValid2, 0)
    assertEquals(semValid2.length, match.matchResult)
    assertEquals(listOf("\t ", "on"), match.tokens)
    assertEquals(listOf("WhitespaceTerminal", "LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = optionalWhitespacePrefix.SemanticMatch(semValid2, 0)
    assertEquals(semValid2.length, match.matchResult)
    assertEquals(listOf("\t ", "on"), match.tokens)
    assertEquals(listOf("WhitespaceTerminal", "LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, optionalWhitespacePrefix.SyntacticMatch(invalid, 0))
    assertEquals(NoMatch, optionalWhitespacePrefix.SemanticMatch(invalid, 0))

    match = optionalWhitespacePrefix.SemanticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("on"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = optionalWhitespacePrefix.SyntacticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("on"), match.tokens)
    assertEquals(listOf("LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = optionalWhitespacePrefix.SemanticMatch(semValid2FromOffset, garbage.length)
    assertEquals(semValid2FromOffset.length, match.matchResult)
    assertEquals(listOf("\t ", "on"), match.tokens)
    assertEquals(listOf("WhitespaceTerminal", "LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    match = optionalWhitespacePrefix.SyntacticMatch(semValid2FromOffset, garbage.length)
    assertEquals(semValid2FromOffset.length, match.matchResult)
    assertEquals(listOf("\t ", "on"), match.tokens)
    assertEquals(listOf("WhitespaceTerminal", "LiteralChoiceTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, optionalWhitespacePrefix.SemanticMatch(invalidFromOffset, garbage.length))
    assertEquals(NoMatch, optionalWhitespacePrefix.SyntacticMatch(invalidFromOffset, garbage.length))
  }

  fun testRepeatCombinatorMatchesNonZeroMin() {
    /**
     * Fixture Setup
     */

    // This combinator should match the options passed in.
    val fizzOrBuzz = RegexTerminal("[a-z]{4}", "fizz|buzz")

    val repeatCombinator = Repeat(fizzOrBuzz, 2, 4)

    val semValid = "fizzbuzzfizz"
    val synValid = "blehblehbleh"
    //val invalid = "fizz"
    val tooShort = "fizz"
    val garbage = "XX"

    val semValidFromOffset = "${garbage}${semValid}"
    val synValidFromOffset = "${garbage}${synValid}"
    val tooShortFromOffset = "${garbage}${tooShort}"

    /**
     * Execute SUT & Verification
     */
    var match = repeatCombinator.SyntacticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("fizz", "buzz", "fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = repeatCombinator.SemanticMatch(semValid, 0)
    assertEquals(semValid.length, match.matchResult)
    assertEquals(listOf("fizz", "buzz", "fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = repeatCombinator.SyntacticMatch(synValid, 0)
    assertEquals(synValid.length, match.matchResult)
    assertEquals(listOf("bleh", "bleh", "bleh"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, repeatCombinator.SemanticMatch(synValid, 0))

    match = repeatCombinator.SyntacticMatch(tooShort, 0)
    assertEquals(-1, match.matchResult)
    assertEquals(emptyList<String>(), match.tokens)
    assertEquals(4, match.longestMatch)

    match = repeatCombinator.SemanticMatch(tooShort, 0)
    assertEquals(-1, match.matchResult)
    assertEquals(emptyList<String>(), match.tokens)
    assertEquals(4, match.longestMatch)

    match = repeatCombinator.SyntacticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("fizz", "buzz", "fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = repeatCombinator.SemanticMatch(semValidFromOffset, garbage.length)
    assertEquals(semValidFromOffset.length, match.matchResult)
    assertEquals(listOf("fizz", "buzz", "fizz"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = repeatCombinator.SyntacticMatch(synValidFromOffset, garbage.length)
    assertEquals(synValidFromOffset.length, match.matchResult)
    assertEquals(listOf("bleh", "bleh", "bleh"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    assertEquals(NoMatch, repeatCombinator.SemanticMatch(synValidFromOffset, garbage.length))

    match = repeatCombinator.SyntacticMatch(tooShortFromOffset, garbage.length)
    assertEquals(-1, match.matchResult)
    assertEquals(emptyList<String>(), match.tokens)
    assertEquals(6, match.longestMatch)

    match = repeatCombinator.SemanticMatch(tooShortFromOffset, garbage.length)
    assertEquals(-1, match.matchResult)
    assertEquals(emptyList<String>(), match.tokens)
    assertEquals(6, match.longestMatch)

  }

  fun testRepeatCombinatorMatchesZeroMinAndExtraAtEnd() {
    /**
     * Fixture Setup
     */

    // This combinator should match the options passed in.
    val fizzOrBuzz = RegexTerminal("[a-z]{4}", "fizz|buzz")

    val repeatCombinator = Repeat(fizzOrBuzz, 0, 2)

    val semValid = "fizzbuzzfizz"
    val synValid = "blehblehbleh"
    //val invalid = "fizz"
    val emptyString = ""
    val garbage = "XX"

    val semValidFromOffset = "${garbage}${semValid}"
    val synValidFromOffset = "${garbage}${synValid}"
    val emptyStringFromOffset = "${garbage}${emptyString}"

    /**
     * Execute SUT & Verification
     */
    var match = repeatCombinator.SyntacticMatch(semValid, 0)
    assertEquals(8, match.matchResult)
    assertEquals(listOf("fizz", "buzz"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = repeatCombinator.SemanticMatch(semValid, 0)
    assertEquals(8, match.matchResult)
    assertEquals(listOf("fizz", "buzz" ), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = repeatCombinator.SyntacticMatch(synValid, 0)
    assertEquals(8, match.matchResult)
    assertEquals(listOf("bleh", "bleh"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = repeatCombinator.SemanticMatch(synValid, 0)
    assertEquals(0, match.matchResult)
    assertEquals(emptyList<String>(), match.tokens)
    assertEquals(0, match.longestMatch)

    match = repeatCombinator.SyntacticMatch(emptyString, 0)
    assertEquals(0, match.matchResult)
    assertEquals(emptyList<String>(), match.tokens)
    assertEquals(0, match.longestMatch)

    match = repeatCombinator.SemanticMatch(emptyString, 0)
    assertEquals(0, match.matchResult)
    assertEquals(emptyList<String>(), match.tokens)
    assertEquals(0, match.longestMatch)

    match = repeatCombinator.SyntacticMatch(semValidFromOffset, garbage.length)
    assertEquals(10, match.matchResult)
    assertEquals(listOf("fizz", "buzz" ), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = repeatCombinator.SemanticMatch(semValidFromOffset, garbage.length)
    assertEquals(10, match.matchResult)
    assertEquals(listOf("fizz", "buzz"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match = repeatCombinator.SyntacticMatch(synValidFromOffset, garbage.length)
    assertEquals(10, match.matchResult)
    assertEquals(listOf("bleh", "bleh"), match.tokens)
    assertEquals(listOf("RegexTerminal", "RegexTerminal"), TerminalTypes(match.terminals))

    match =  repeatCombinator.SemanticMatch(synValidFromOffset, garbage.length)
    assertEquals(2, match.matchResult)
    assertEquals(emptyList<String>(), match.tokens)
    assertEquals(0, match.longestMatch)

    match = repeatCombinator.SyntacticMatch(emptyStringFromOffset, garbage.length)
    assertEquals(2, match.matchResult)
    assertEquals(emptyList<String>(), match.tokens)
    assertEquals(0, match.longestMatch)

    match = repeatCombinator.SemanticMatch(emptyStringFromOffset, garbage.length)
    assertEquals(2, match.matchResult)
    assertEquals(emptyList<String>(), match.tokens)
    assertEquals(0, match.longestMatch)

  }

}
