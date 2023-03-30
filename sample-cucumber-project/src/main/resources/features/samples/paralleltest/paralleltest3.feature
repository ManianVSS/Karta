@parallelTest
Feature: Test parallel feature3
  Narrative: Test parallel feature3

  Scenario: Test parallel f3scenario1
    Given print "Hello world 1"
    Then sleep 1000

  Scenario: Test parallel f3scenario2
    Given print "Hello world 2"
    Then sleep 2000

  Scenario: Test parallel f3scenario3
    Given print "Hello world 3"
    Then sleep 3000

  Scenario: Test parallel f3scenario4
    Given print "Hello world 4"
    Then sleep 4000
