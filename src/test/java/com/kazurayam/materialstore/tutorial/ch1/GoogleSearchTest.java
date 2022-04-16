package com.kazurayam.materialstore.tutorial.ch1;

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
        WebDriverManager.chromedriver().setup();    // <1>
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(GoogleSearchTest.class.getName());
        Files.createDirectories(outputDir);    // <2>
        Path root = outputDir.resolve("store");    // <3>
        store = Stores.newInstance(root);    // <4>
    }

    @BeforeEach
    public void beforeEach() {
        // <5>
        jobName = new JobName("GoogleSearch");
        jobTimestamp = JobTimestamp.now();

        driver = new ChromeDriver(); // <6>
    }

    @Test
    public void test_google_search() throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, 10);

        // <7>
        URL entryURL = new URL("https://www.google.com");
        driver.navigate().to(entryURL);

        // <8>
        By by_input_q = By.cssSelector("input[name=\"q\"]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(by_input_q));
        WebElement we_input_q = driver.findElement(by_input_q);
        String qValue = "Shohei Ohtani";
        we_input_q.sendKeys(qValue);

        // <9>
        TakesScreenshot scrShot = (TakesScreenshot) driver;
        File tempFile1 = scrShot.getScreenshotAs(OutputType.FILE);

        // <10>
        Metadata metadata =
                Metadata.builder(entryURL)
                        .put("step", "1")    // remember the step sequence
                        .put("q", qValue)    // remember the query string
                        .build();
        store.write(jobName, jobTimestamp, FileType.PNG, metadata, tempFile1);

        // send ENTER to execute a search request;
        // then browser transitions to the Search Result page
        we_input_q.sendKeys(Keys.chord(Keys.ENTER));

        // wait for the Search Result page to load completely
        By by_img_logo = By.xpath("//div[contains(@class,'logo')]/a/img");
        wait.until(ExpectedConditions.visibilityOfElementLocated(by_img_logo));

        // take a screenshot of the Search Result page
        File tempFile2 = scrShot.getScreenshotAs(OutputType.FILE);

        // save the image into the store
        URL resultPageURL = new URL(driver.getCurrentUrl());
        Metadata metadata2 =
                Metadata.builder(resultPageURL)
                        .put("step", "2")   // this is the 2nd step
                        .build();
        store.write(jobName, jobTimestamp, FileType.PNG, metadata2, tempFile2);

        // Now I want to compile a report in HTML
        // get the list of files stored in the storage directory
        MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);

        // compile the report
        Inspector inspector = Inspector.newInstance(store);
        String fileName = jobName.toString() + "-list.html";
        Path report = inspector.report(materialList, fileName);
        System.out.println("The report will be found at " + report.toString());
    }

    @AfterEach
    public void afterEach() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
