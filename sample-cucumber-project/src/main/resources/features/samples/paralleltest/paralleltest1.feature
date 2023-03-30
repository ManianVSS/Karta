@parallelTest
Feature: Test parallel feature1
  Narrative: Test parallel feature1

  Scenario: Test parallel f1scenario1
    Given print "Hello world 1"
    Then sleep 1000

  Scenario: Test parallel f1scenario2
    Given print "Hello world 2"
    Then sleep 2000

  Scenario: Test parallel f1scenario3
    Given print "Hello world 3"
    Then sleep 3000

  Scenario: Test parallel f1scenario4
    Given print "Hello world 4"
    Then sleep 4000
