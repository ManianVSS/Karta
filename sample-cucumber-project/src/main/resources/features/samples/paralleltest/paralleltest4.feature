@parallelTest
Feature: Test parallel feature4
  Narrative: Test parallel feature4

  Scenario: Test parallel f4scenario1
    Given print "Hello world 1"
    Then sleep 1000

  Scenario: Test parallel f4scenario2
    Given print "Hello world 2"
    Then sleep 2000

  Scenario: Test parallel f4scenario3
    Given print "Hello world 3"
    Then sleep 3000

  Scenario: Test parallel f4scenario4
    Given print "Hello world 4"
    Then sleep 4000
