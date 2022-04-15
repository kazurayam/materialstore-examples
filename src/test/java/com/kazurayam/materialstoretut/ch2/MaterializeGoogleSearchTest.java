package com.kazurayam.materialstoretut.ch2;

import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
public class MaterializeGoogleSearchTest {

    private static Path outputDir;
    private static Store store;
    private JobName jobName;
    private JobTimestamp jobTimestamp;
    private WebDriver driver;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(MaterializeGoogleSearchTest.class.getName());
        Files.createDirectories(outputDir);
        //
        WebDriverManager.chromedriver().setup();
        //
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
    public void test_google_for_Shohei_Ohtani() throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        URL entryURL = new URL("https://www.google.com");
        driver.navigate().to(entryURL);
        By by_input_q = By.cssSelector("input[name=\"q\"]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(by_input_q));
        WebElement we_input_q = driver.findElement(by_input_q);
        if (we_input_q == null) throw new IllegalStateException();
    }

    @AfterEach
    public void afterEach() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
