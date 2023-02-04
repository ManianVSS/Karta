package mysample.stepdefinitions;

import java.util.HashMap;

import org.mvss.karta.framework.core.ContextBean;
import org.mvss.karta.framework.core.StepDefinition;
import org.mvss.karta.framework.enums.StepOutputType;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.openqa.selenium.WebDriver;

import mysample.pom.W3SLearnHTMLHomePage;
import mysample.pom.W3SLearnHTMLIntroPage;
import mysample.pom.W3SchoolsHomePage;

public class UIStepDefinitions {
    @PropertyMapping(group = "WebAutomation", value = "startURL")
    private String startURL;

    @PropertyMapping(group = "WebAutomation", value = "xpaths")
    private HashMap<String, String> xpathMap;

    @StepDefinition(value = "the W3schools web application is launched", outputType = StepOutputType.BEAN, outputName = "currentPage")
    public W3SchoolsHomePage the_W3schools_web_application_is_launched(@ContextBean("WebDriverObject") WebDriver driver) throws TestFailureException {
        driver.navigate().to(startURL);
        return new W3SchoolsHomePage(driver);
    }

    @StepDefinition(value = "the learn html button is clicked", outputType = StepOutputType.BEAN, outputName = "currentPage")
    public W3SLearnHTMLHomePage the_learn_html_button_is_clicked(@ContextBean("currentPage") W3SchoolsHomePage w3SchoolsHomePage) throws TestFailureException {
        return w3SchoolsHomePage.clickOnLeanHTML();
    }

    @StepDefinition(value = "the next button is clicked on the learn html home page", outputType = StepOutputType.BEAN, outputName = "currentPage")
    public W3SLearnHTMLIntroPage the_next_button_is_clicked_on_learn_html_home_page(@ContextBean("currentPage") W3SLearnHTMLHomePage w3sLearnHTMLHomePage) throws TestFailureException {
        return w3sLearnHTMLHomePage.clickOnNextButton();
    }

    @StepDefinition(value = "the home button is clicked on the learn html home page", outputType = StepOutputType.BEAN, outputName = "currentPage")
    public W3SchoolsHomePage the_home_button_is_clicked_on_the_learn_html_home_page(@ContextBean("currentPage") W3SLearnHTMLHomePage w3sLearnHTMLHomePage) throws TestFailureException {
        return w3sLearnHTMLHomePage.clickOnHomeButton();
    }

    @StepDefinition(value = "the previous button is clicked on the learn html intro page", outputType = StepOutputType.BEAN, outputName = "currentPage")
    public W3SLearnHTMLHomePage the_previous_button_is_clicked_on_the_learn_html_intro_page(@ContextBean("currentPage") W3SLearnHTMLIntroPage w3sLearnHTMLIntroPage) throws TestFailureException {
        return w3sLearnHTMLIntroPage.clickOnPreviousButton();
    }

    @StepDefinition(value = "the home button is clicked on the learn html intro page", outputType = StepOutputType.BEAN, outputName = "currentPage")
    public W3SchoolsHomePage the_home_button_is_clicked_on_the_learn_html_intro_page(@ContextBean("currentPage") W3SLearnHTMLIntroPage w3sLearnHTMLIntroPage) throws TestFailureException {
        return w3sLearnHTMLIntroPage.clickOnHomeButton();
    }

}
