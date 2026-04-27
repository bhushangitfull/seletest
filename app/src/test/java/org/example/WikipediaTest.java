package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import org.testng.ITestResult;






public class WikipediaTest {

    WebDriver driver;
    WebDriverWait wait;
    static final String BASE_URL = "https://www.wikipedia.org";
     static final String SCREENSHOT_DIR = "screenshots/";

    // ─────────────────────────────────────────
    //  SETUP & TEARDOWN
    // ─────────────────────────────────────────

    @BeforeClass
    public void setup() {
        new File(SCREENSHOT_DIR).mkdirs();
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterClass
    public void teardown() {
        if (driver != null) driver.quit();
    }

    @BeforeMethod
    public void goHome() {
        driver.get(BASE_URL);
    }

    @AfterMethod
    public void captureScreenshot(ITestResult result) {
        String status = result.getStatus() == ITestResult.FAILURE ? "FAILED_" : "PASSED_";
        takeScreenshot(status + result.getName());
    }

    public void takeScreenshot(String testName) {
    try {
        TakesScreenshot ts = (TakesScreenshot) driver;
        File src = ts.getScreenshotAs(OutputType.FILE);

        // Save to screenshots folder with timestamp
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String path = "screenshots/" + testName + "_" + timestamp + ".png";

        FileUtils.copyFile(src, new File(path));
        System.out.println("📸 Screenshot saved: " + path);

    } catch (IOException e) {
        System.out.println("❌ Screenshot failed: " + e.getMessage());
    }
}

    // ─────────────────────────────────────────
    //  TEST 1: Homepage Loads Correctly
    // ─────────────────────────────────────────

    @Test(priority = 1, description = "Verify Wikipedia homepage loads with correct title")
    public void testHomepageTitle() {
        String title = driver.getTitle();
        System.out.println("✅ Page Title: " + title);
        Assert.assertTrue(title.contains("Wikipedia"),
                "Title should contain 'Wikipedia' but was: " + title);
    }

    // ─────────────────────────────────────────
    //  TEST 2: Search Bar is Visible
    // ─────────────────────────────────────────

    @Test(priority = 2, description = "Verify search bar is present on homepage")
    public void testSearchBarVisible() {
        WebElement searchBox = driver.findElement(By.id("searchInput"));
        Assert.assertTrue(searchBox.isDisplayed(), "Search bar should be visible");
        System.out.println("✅ Search bar is visible");
    }

    // ─────────────────────────────────────────
    //  TEST 3: Search for a Topic
    // ─────────────────────────────────────────

    @Test(priority = 3, description = "Search for 'Artificial Intelligence' and verify article loads")
    public void testSearchFunctionality() {
        WebElement searchBox = driver.findElement(By.id("searchInput"));
        searchBox.clear();
        searchBox.sendKeys("Artificial Intelligence");
        searchBox.sendKeys(Keys.ENTER);

        wait.until(ExpectedConditions.titleContains("Artificial intelligence"));

        String pageTitle = driver.getTitle();
        System.out.println("✅ Search Result Page Title: " + pageTitle);
        Assert.assertTrue(pageTitle.toLowerCase().contains("artificial intelligence"),
                "Should land on AI article, but title was: " + pageTitle);
    }

    // ─────────────────────────────────────────
    //  TEST 4: Article Has Content Sections
    // ─────────────────────────────────────────

    @Test(priority = 4, description = "Verify article page has a body content section")
    public void testArticleHasContent() {
        // Navigate directly to a stable article
        driver.get("https://en.wikipedia.org/wiki/Python_(programming_language)");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mw-content-text")));

        WebElement content = driver.findElement(By.id("mw-content-text"));
        Assert.assertTrue(content.isDisplayed(), "Article content section should be visible");

        String bodyText = content.getText();
        Assert.assertFalse(bodyText.isEmpty(), "Article content should not be empty");
        System.out.println("✅ Article content loaded. First 100 chars: " + bodyText.substring(0, 100));
    }

    // ─────────────────────────────────────────
    //  TEST 5: Article Has a Table of Contents
    // ─────────────────────────────────────────

    @Test(priority = 5, description = "Verify article Table of Contents (TOC) is present")
    public void testArticleHasTableOfContents() {
        driver.get("https://en.wikipedia.org/wiki/Java_(programming_language)");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mw-content-text")));

        // TOC is present in major articles
        boolean hasTOC = !driver.findElements(By.id("toc")).isEmpty()
                      || !driver.findElements(By.cssSelector(".mw-table-of-contents")).isEmpty()
                      || !driver.findElements(By.cssSelector("[class*='toc']")).isEmpty();

        Assert.assertTrue(hasTOC, "Article should contain a Table of Contents");
        System.out.println("✅ Table of Contents found on Java article");
    }

    // ─────────────────────────────────────────
    //  TEST 6: Navigate via Internal Article Link
    // ─────────────────────────────────────────

    @Test(priority = 6, description = "Click an internal Wikipedia link and verify navigation works")
    public void testInternalLinkNavigation() {
        driver.get("https://en.wikipedia.org/wiki/Selenium_(software)");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mw-content-text")));

        // Find any internal Wikipedia link inside the article body
        WebElement internalLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#mw-content-text a[href^='/wiki/']:not([href*=':'])")));

        String linkText = internalLink.getText();
        System.out.println("🔗 Clicking internal link: " + linkText);
        internalLink.click();

        wait.until(ExpectedConditions.urlContains("/wiki/"));

        String newUrl = driver.getCurrentUrl();
        Assert.assertTrue(newUrl.contains("wikipedia.org/wiki/"),
                "Should navigate to another Wikipedia article. URL: " + newUrl);
        System.out.println("✅ Navigated to: " + newUrl);
    }



    // ─────────────────────────────────────────
    //  TEST 8: Navigate to English Wikipedia & Verify Logo
    // ─────────────────────────────────────────

    @Test(priority = 8, description = "Navigate to English Wikipedia and verify logo is present")
    public void testEnglishWikipediaLogo() {
        driver.get("https://en.wikipedia.org/");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mw-content-text")));

        // Logo should be present
        WebElement logo = driver.findElement(By.cssSelector("#p-logo a, .mw-logo, a[class*='logo']"));
        Assert.assertTrue(logo.isDisplayed(), "Wikipedia logo should be visible");
        System.out.println("✅ Wikipedia logo is visible on English homepage");
    }

    // ─────────────────────────────────────────
    //  TEST 9: Search Returns No Result Gracefully
    // ─────────────────────────────────────────

    @Test(priority = 9, description = "Verify Wikipedia handles a non-existent search gracefully")
    public void testSearchNoResults() {
        driver.get("https://en.wikipedia.org/");

        WebElement searchBox = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("searchInput")));
        searchBox.clear();
        searchBox.sendKeys("xyzzy12345nonexistentarticleqwerty");
        searchBox.sendKeys(Keys.ENTER);

        // Wikipedia shows a "no results" message or a search results page
        wait.until(ExpectedConditions.or(
                ExpectedConditions.titleContains("Search results"),
                ExpectedConditions.urlContains("Special:Search"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".searchresults, .mw-search-nonexistent"))
        ));

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("wikipedia.org"),
                "Should stay on Wikipedia after empty search");
        System.out.println("✅ Wikipedia handled non-existent search gracefully. URL: " + currentUrl);
    }

    // ─────────────────────────────────────────
    //  TEST 10: Article Page Has Edit Section Button (Logged-out users see it)
    // ─────────────────────────────────────────

    @Test(priority = 10, description = "Verify 'Edit' link appears on a Wikipedia article")
    public void testArticleHasEditLink() {
        driver.get("https://en.wikipedia.org/wiki/India");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mw-content-text")));

        // Edit tab in the top navigation
        boolean editLinkPresent = !driver.findElements(
                By.cssSelector("#ca-edit a, #ca-viewsource a, [id^='ca-edit']")).isEmpty();

        Assert.assertTrue(editLinkPresent, "Edit or View Source link should be present on article");
        System.out.println("✅ Edit/View Source link found on India article");
    }
}