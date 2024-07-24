package org.mvss.karta.framework.web;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.Constants;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;

@Getter
@Log4j2
@SuppressWarnings("unused")
public class WebDriverWrapper implements AutoCloseable {
    public static final String XPATH = "xpath";
    public static final String ID = "id";
    public static final String CLASS_NAME = "class name";
    public static final String LINK_TEXT = "link text";
    public static final String CSS = "css";
    public static final String CLASS_NAME1 = "className";
    public static final String LINK_TEXT1 = "linkText";
    public static final String PARTIAL_LINK_TEXT = "partialLinkText";
    public static final String TAG_NAME = "tagName";

    protected final WebDriverWait wait;
    protected final WebDriverWait waitLonger;
    protected final WebDriverWait negativeWait;
    protected WebDriver driver;

    public WebDriverWrapper(WebDriver driver, Duration objectTimeout, Duration longerObjectTimeout) {
        this.driver = driver;
        wait = new WebDriverWait(driver, objectTimeout);
        waitLonger = new WebDriverWait(driver, longerObjectTimeout);
        negativeWait = new WebDriverWait(driver, Duration.of(0, ChronoUnit.SECONDS));
    }

    public byte[] getScreenShot() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    public byte[] getScreenShot(WebElement element) {
        return element.getScreenshotAs(OutputType.BYTES);
    }

    public void captureScreenShot(String prefix) throws IOException {
        FileUtils.writeByteArrayToFile(new File(prefix + Constants.UNDERSCORE + System.currentTimeMillis() + Constants.PNG), getScreenShot());
    }

    public void captureScreenShot(WebElement element, String fileName) throws IOException {
        FileUtils.writeByteArrayToFile(new File(fileName), getScreenShot(element));
    }

    public static void takeSnapshot(WebDriver driver, String fileName) throws IOException {
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.YYYY_MM_DD_HH_MM_SS);
        UUID uuid = UUID.randomUUID();
        FileUtils.copyFile(srcFile, new File(fileName + Constants.HYPHEN + dateFormat.format(date) + Constants.HYPHEN + uuid + Constants.PNG));
    }

    @Override
    public void close() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public SearchContext getShadowRootContext(By... rootBys) {
        SearchContext shadowRootContext = driver;
        if (rootBys != null) {
            for (By rootBy : rootBys) {
                shadowRootContext = shadowRootContext.findElement(rootBy).getShadowRoot();
            }
        }
        return shadowRootContext;
    }

    public WebElement navigateShadowRootsAndLocate(By by, By... rootBys) {
        if (by == null) {
            return null;
        }
        SearchContext shadowRootContext = getShadowRootContext(rootBys);
        return shadowRootContext.findElement(by);
    }

    public List<WebElement> navigateShadowRootsAndLocateElements(By by, By... rootBys) {
        if (by == null) {
            return null;
        }
        SearchContext shadowRootContext = getShadowRootContext(rootBys);
        return shadowRootContext.findElements(by);
    }

    public By getByFindBy(FindBy findBy) {
        By by = null;
        if (StringUtils.isNotEmpty(findBy.id())) {
            by = By.id(findBy.id());
        } else if (StringUtils.isNotEmpty(findBy.name())) {
            by = By.name(findBy.name());
        } else if (StringUtils.isNotEmpty(findBy.className())) {
            by = By.className(findBy.className());
        } else if (StringUtils.isNotEmpty(findBy.css())) {
            by = By.cssSelector(findBy.css());
        } else if (StringUtils.isNotEmpty(findBy.tagName())) {
            by = By.tagName(findBy.tagName());
        } else if (StringUtils.isNotEmpty(findBy.linkText())) {
            by = By.linkText(findBy.linkText());
        } else if (StringUtils.isNotEmpty(findBy.partialLinkText())) {
            by = By.partialLinkText(findBy.partialLinkText());
        } else if (StringUtils.isNotEmpty(findBy.xpath())) {
            by = By.xpath(findBy.xpath());
        }
        return by;
    }

    public void initElements(Object page) {
        // Call Selenium initElements first
        PageFactory.initElements(driver, page);

        for (Field field : this.getClass().getFields()) {
            if (WebElement.class.isAssignableFrom(field.getType())) {
                FindByWrapper findInShadowRootParentBy = field.getAnnotation(FindByWrapper.class);

                if (findInShadowRootParentBy == null) {
                    continue;
                }

                FindBy findBy = findInShadowRootParentBy.value();

                By by = getByFindBy(findBy);
                if (by == null) {
                    continue;
                }

                FindBy[] shadowRoots = findInShadowRootParentBy.shadowRoots();

                if ((shadowRoots == null) || (shadowRoots.length == 0)) {
                    try {
                        field.set(this, driver.findElement(by));
                    } catch (IllegalAccessException iea) {
                        return;
                    } catch (Throwable t) {
                        // continue
                    }
                } else {
                    By[] rootBys = new By[shadowRoots.length];

                    boolean rootByNotFound = false;
                    for (int i = 0; i < shadowRoots.length; i++) {
                        rootBys[i] = getByFindBy(shadowRoots[i]);

                        if (rootBys[i] == null) {
                            rootByNotFound = true;
                            break;
                        }
                    }

                    if (rootByNotFound) {
                        continue;
                    }

                    try {
                        field.set(this, navigateShadowRootsAndLocate(by, rootBys));
                    } catch (IllegalAccessException iea) {
                        return;
                    } catch (Throwable t) {
                        // continue;
                    }
                }
            }
        }
    }

    public WebElement expandRootElement(WebElement element) {
        return (WebElement) ((JavascriptExecutor) driver).executeScript("return arguments[0].shadowRoot", element);
    }

    public String getCurrentURL() {
        return driver.getCurrentUrl();
    }


    public void navigateToURL(String URL) {
        driver.get(URL);
    }

    public void navigateTo(String URL) {
        driver.navigate().to(URL);
    }

    public boolean javascriptExecutor(String argument, WebElement ele) {
        try {
            ((JavascriptExecutor) driver).executeScript(argument, ele);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void clickElement(WebElement element) {
        waitForObjectToAppear(element);
        waitForObjectTobeClickable(element);
        element.click();
    }

    public boolean isElementAvailable(WebElement element) {
        try {
            waitForObjectToAppear(element);
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementUnavailable(WebElement element) {
        try {
            return negativeWaitForObjectToAppear(element);
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isElementEnabled(WebElement element) {
        try {
            waitForObjectToAppear(element);
            return element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public void sendKeys(WebElement element, CharSequence... keys) {
        waitForObjectToAppear(element);
        waitForObjectTobeClickable(element);
        element.sendKeys(keys);
    }

    public void clearAndSendKeys(WebElement element, CharSequence... keys) {
        waitForObjectToAppear(element);
        element.clear();
        element.sendKeys(keys);
    }

    public void setTextBox(WebElement element, String text) {
        waitForObjectToAppear(element);
        waitForObjectTobeClickable(element);
        element.sendKeys(text);
    }

    public String getText(WebElement element) {
        waitForObjectToAppear(element);
        return element.getText();
    }

    public String getTextByValueAttribute(WebElement element) {
        waitForObjectToAppear(element);
        return element.getAttribute("value");

    }

    public boolean selectComboBoxItem(WebElement element, String itemToSelect) {
        Select dropDown = new Select(element);
        List<WebElement> Options = dropDown.getOptions();
        for (WebElement option : Options) {
            if (option.getText().equals(itemToSelect)) {
                option.click();
                return true;
            }
        }
        return false;
    }

    public List<WebElement> getComboBoxItems(WebElement element) {
        Select dropDown = new Select(element);
        return dropDown.getOptions();
    }

    public WebElement getComboBoxSelectedItem(WebElement element) {
        return new Select(element).getFirstSelectedOption();
    }

    public WebElement prepareWebElementById(String idVal, String oldValue, String newValue) {
        return driver.findElement(By.id(idVal.replace(oldValue, newValue)));
    }

    public int getTableSize(String stringXpath) {
        return driver.findElements(By.xpath(stringXpath)).size();
    }

    public List<WebElement> getElementsByXPath(String stringXpath) {
        return driver.findElements(By.xpath(stringXpath));
    }

    private static By getByLocatorType(String locatorString, String locatorType) {
        By locator;
        locator = switch (locatorType) {
            case XPATH -> By.xpath(locatorString);
            case ID -> By.id(locatorString);
            case CLASS_NAME1 -> By.className(locatorString);
            case LINK_TEXT1 -> By.linkText(locatorString);
            case PARTIAL_LINK_TEXT -> By.partialLinkText(locatorString);
            case TAG_NAME -> By.tagName(locatorString);
            case CSS -> By.cssSelector(locatorString);
            default -> null;
        };
        return locator;
    }


    public WebElement getElementBy(String locatorType, String locatorString) {
        By locator = getByLocatorType(locatorString, locatorType);
        if (locator == null) {
            return null;
        }
        return driver.findElement(locator);
    }

    public List<WebElement> getElementsBy(String locatorType, String locatorString) {
        By locator = getByLocatorType(locatorString, locatorType);
        if (locator == null) {
            return null;
        }
        return driver.findElements(locator);
    }


    public WebElement findElementByLocatorString(String locatorString, String locatorType, HashMap<String, String> replaceValuesMap) {
        By locator;
        Iterator<Entry<String, String>> it = replaceValuesMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            locatorString = locatorString.replace(entry.getKey(), entry.getValue().trim());
            it.remove();
        }
        locator = getByLocatorType(locatorString, locatorType);
        waitForLocatorToVisible(locator);
        return driver.findElement(locator);
    }


    public By prepareByLocator(String locatorString, String locatorType, HashMap<String, String> replaceValuesMap) {
        By locator;
        Iterator<Entry<String, String>> it = replaceValuesMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            locatorString = locatorString.replace(entry.getKey(), entry.getValue().trim());
            it.remove();
        }
        locator = getByLocatorType(locatorString, locatorType);
        waitForLocatorToVisible(locator);
        return locator;
    }


    public List<WebElement> prepareWebElementsByLocator(String locatorString, String locatorType, HashMap<String, String> replaceValuesMap) {
        By locator;
        Iterator<Entry<String, String>> it = replaceValuesMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            locatorString = locatorString.replace(entry.getKey(), entry.getValue().trim());
            it.remove();
        }
        locator = getByLocatorType(locatorString, locatorType);
        waitForLocatorToVisible(locator);
        return driver.findElements(locator);
    }

    public void clickMultipleWebElements(List<By> byEleLst) {
        Actions builder = new Actions(driver);
        WebElement element;
        for (int i = 0; i < byEleLst.size(); i++) {
            element = waitForLocatorTobeClickable(byEleLst.get(i));
            if (i == 0) {
                builder.click(element).keyDown(Keys.CONTROL).build().perform();
            } else if (i == byEleLst.size() - 1) {
                builder.click(element).keyUp(Keys.CONTROL).build().perform();
            } else {
                element.click();
            }

        }
    }

    public void clearTextBox(WebElement element) {
        waitForObjectToAppear(element);
        element.clear();
    }

    public WebElement prepareWebElementUsingXpath(String xpathVal, String oldValue, String newValue) {
        return driver.findElement(By.xpath(xpathVal.replace(oldValue, newValue)));

    }

    public void waitForObjectToAppear(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    public void waitForFrameToAppear(WebElement element) {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(element));
    }

    public void waitForFrameToAppear(By frameLocator) {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
    }

    public void waitForFrameToAppear(String frameLocator) {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
    }

    /**
     * @param element WebElement to wait for
     * @return true if element not found instantly else returns false
     */
    public boolean negativeWaitForObjectToAppear(WebElement element) {
        try {
            negativeWait.until(ExpectedConditions.visibilityOf(element));
            return false;
        } catch (TimeoutException runtimeException) {
            return true;
        }
    }

    public void waitForObjectToDisappear(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    public void takeSnapshot(String name) throws IOException {
        takeSnapshot(driver, name);
    }

    public void waitForObjectTobeClickable(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public void waitForLocatorToVisible(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForLocatorTobeClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public void waitForCSSClassToBeApplied(WebElement element, String cssClass) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(cssClass)));
    }

    public void waitLongerForObjectToAppear(WebElement element) {
        waitLonger.until(ExpectedConditions.visibilityOf(element));
    }

    public void waitForElementTextToChangeTo(WebElement element, String text) {
        wait.until(ExpectedConditions.textToBePresentInElement(element, text));
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public void switchToFrame(String nameOrId) {
        waitForFrameToAppear(nameOrId);
    }

    public void switchToFrame(WebElement element) {
        waitForFrameToAppear(element);
    }

    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    public boolean javascriptClick(WebElement ele) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ele);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public void scrollElementIntoView(WebElement ele) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", ele);
    }

    public void scrollToBottomOfWebPage() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    public WebElement prepareWebElementUsingCSS(String cssVal, String oldValue, String newValue) {
        return driver.findElement(By.cssSelector(cssVal.replace(oldValue, newValue)));
    }

    public void sendEscapeKey() {
        Actions action = new Actions(driver);
        action.sendKeys(Keys.ESCAPE).build().perform();
    }

    public void sendTabKey() {
        Actions action = new Actions(driver);
        action.sendKeys(Keys.TAB).build().perform();
    }

    public WebElement findElement(String xPathValue) {
        return driver.findElement(By.xpath(xPathValue));
    }

    public String getParentWindowHandle() {
        return driver.getWindowHandle();
    }

    public void switchToParentWindow(String adminConsoleWindowHandle) {
        driver.switchTo().window(adminConsoleWindowHandle);
        driver.switchTo().defaultContent();
    }

    public void switchToFrameByWebElement(WebElement we) {
        driver.switchTo().frame(we);
    }

    public ArrayList<String> getWindowHandles() {
        return new ArrayList<>(driver.getWindowHandles());
    }

    public void implicitWaitWrapper(Duration time) {
        driver.manage().timeouts().implicitlyWait(time);
    }

    public void switchToFrameByIndex(int index) {
        driver.switchTo().frame(index);
    }

    public void switchToWindow(int index, ArrayList<String> windowHandles) {
        driver.switchTo().window(windowHandles.get(index));
    }

    public List<WebElement> findElementsByXpath(By xPathValue) {
        return driver.findElements(xPathValue);
    }

    public Set<Cookie> getCookies() {
        return driver.manage().getCookies();
    }

    public String getCookie(String name) {
        return driver.manage().getCookieNamed(name).getValue();
    }
}
