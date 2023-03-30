@parallelTest
Feature: Test parallel feature2
  Narrative: Test parallel feature2

  Scenario: Test parallel f2scenario1
    Given print "Hello world 1"
    Then sleep 1000

  Scenario: Test parallel f2scenario2
    Given print "Hello world 2"
    Then sleep 2000

  Scenario: Test parallel f2scenario3
    Given print "Hello world 3"
    Then sleep 3000

  Scenario: Test parallel f2scenario4
    Given print "Hello world 4"
    Then sleep 4000
