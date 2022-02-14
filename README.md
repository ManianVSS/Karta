# Karta
Karta is a software system test automation framework built ground up for system testing.

Features:
- BDD/Java Tests: YAML based feature files, plugin support for Gherkin or legacy keyword driven tests, easily import/migrate from Cucumber/Test NG.
- Web Automation:  OOB support for using Selenium Web-driver page object model.
- API Automation:  Abstraction of Rest Assured and Apache HTTP client libraries.
- Configuration management/Properties: YAML/JSON/XML/Java properties files. supports complex. data types, multiple files, overriding, support for Containerized/Kubernetes/Cloud environments.
- Dependency Injection: Inject properties/DTO and objects/beans directly into POJO without parsing.
- Scalability: Load balance multiple test nodes, run features(tests), scenarios or steps in parallel threads.
- Remote execution: Run any step on any test node and pass data in between seamlessly.
- Variability Support: Test scenario variability, Test data variability, Variable Object generation etc.
- Reliability testing: Cycle testing/Growth tests(run million iterations), Longevity testing (run for months).
- Chaos Testing: Declarative YAML Configuration, nesting chaos actions, variability, Chaos for any layer.
- Extensibility: Custom plugin for feature parser, step runner, test data source, test life cycle hook and test event listener.
- Reporting: Out of box HTML report, support to implement test event listeners to send data to any reporting tools/dashboard like Allure, Extent, Report Portal etc.
- API Server: API server to trigger tests/scenarios/steps using REST API.
- Packaging: Support to package tests as run-able jar/artifacts, Docker containers, Helm charts.
- Deploy Anywhere: Support to run on any OS with just JRE, run containers/charts in docker/K8s.
- CI/CD: Seamless integration with trigger options like run-able jar/API/maven trigger.