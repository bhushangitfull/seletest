package org.example;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;  
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

public class OpenSiteTest {
    WebDriver driver;

    @BeforeTest
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void testOpenSite() {
        driver.get("https://jp-anime-learner.vercel.app/");
        System.out.println("Page Title: " + driver.getTitle());
        Assert.assertNotNull(driver.getTitle());
        System.out.println("✅ Site opened successfully!");
        
    }

    @AfterTest
    public void teardown() {
        
        driver.quit();
    }
}
