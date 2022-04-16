package com.kazurayam.materialstore.tutorial.ch1introduction;

import com.kazurayam.materialstore.Inspector;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A Selenium Test that does the following:
 * 1. open Chrome browser
 * 2. visit "https://www.google.com/
 * 3. make a search for "Shohei Ohtani"
 * 4. take screenshots of the screen using AShot
 * 5. download HTML source of the web pages pages
 * 6. store the PNG and HTML files into the materialstore
 */
public class GoogleSearchTest {

    private static Path outputDir;
    private static Store store;
    private JobName jobName;
    private JobTimestamp jobTimestamp;
    private WebDriver driver;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(GoogleSearchTest.class.getName());
        Files.createDirectories(outputDir);
        WebDriverManager.chromedriver().setup();
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
    }

    @BeforeEach
    public void beforeEach() {
        jobName = new JobName("GoogleSearch");
        jobTimestamp = JobTimestamp.now();
        driver = new ChromeDriver();
    }

    @Test
    public void test_google_search() throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        // open the Google Search page
        URL entryURL = new URL("https://www.google.com");
        driver.navigate().to(entryURL);
        // set a query into the <input name="q">
        By by_input_q = By.cssSelector("input[name=\"q\"]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(by_input_q));
        WebElement we_input_q = driver.findElement(by_input_q);
        String qValue = "Shohei Ohtani";
        we_input_q.sendKeys(qValue);
        // take the screenshot of the Google Search page,
        TakesScreenshot scrShot = (TakesScreenshot) driver;
        File srcFile1 = scrShot.getScreenshotAs(OutputType.FILE);
        // save the image into the store
        Metadata metadata =
                Metadata.builder(entryURL)
                        .put("step", "1")
                        .put("q", qValue)
                        .build();
        store.write(jobName, jobTimestamp, FileType.PNG, metadata, srcFile1);
        // send ENTER to execute a search request
        we_input_q.sendKeys(Keys.chord(Keys.ENTER));
        // wait for the search result page to load
        By by_img_logo = By.xpath("//div[contains(@class,'logo')]/a/img");
        wait.until(ExpectedConditions.visibilityOfElementLocated(by_img_logo));
        // take screenshot of the result page, store the image into the store
        File srcFile2 = scrShot.getScreenshotAs(OutputType.FILE);
        // save the image into the store
        URL resultPageURL = new URL(driver.getCurrentUrl());
        Metadata metadata2 = Metadata.builder(resultPageURL).put("step", "2").build();
        store.write(jobName, jobTimestamp, FileType.PNG, metadata2, srcFile2);
        // get the MaterialList
        MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        // compile the report
        Inspector inspector = Inspector.newInstance(store);
        String fileName = jobName.toString() + "-list.html";
        Path report = inspector.report(materialList, fileName);
        System.out.println("report found at " + report.toString());
    }

    @AfterEach
    public void afterEach() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
