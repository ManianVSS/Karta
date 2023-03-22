@ParsedFeature
@AdditionalTag1
Feature: GherkinCalculatorTest2
  This is a sample gherkin feature file being parsed into test catalog automatically.

  Background:
    When the all clear button is pressed
    Then the calculator should display "0"

  Scenario Outline: Scenario1
    When the button "1" is pressed
    And the button "+" is pressed
    And the button "2" is pressed
    And the button "=" is pressed
    And the calculator should display "3"

    Examples:
      | str1 | str2 | str3 |
      | a    | b    | c    |
      | d    | e    | f    |

  Scenario: Scenario2
    When a binary operation is perfomed on the calculator
      | operand1 | operation | operand2 |
      | 100      | "/"       | 200      |
    And the calculator should display "0.5"

