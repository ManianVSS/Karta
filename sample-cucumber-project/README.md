# Sample Cucumber Project with Karta

This is an example project to show how Karta Dependency injection be used in existing Cucumber project.

Usage Steps:

- To run the code as IntelliJ IDE run configuration run io.cucumber.core.cli.Main similar to runTests.bat from
  target/classes.
  <br> or create junit Test run configuration with TestRunner and run with environment variable
  PROPERTIES_FOLDER=../classes/properties from the target/test-classes folder.

- To run without IDE, do maven build from karta and sample-cucumber-project.
  <br> Then run from target/packages/sample-cucumber-project <br>
  ./runTest.sh @parallelTest
