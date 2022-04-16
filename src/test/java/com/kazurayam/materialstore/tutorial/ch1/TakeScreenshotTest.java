package com.kazurayam.materialstoretut.ch1;

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
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Guru99, How to Take Screenshot in Selenium WebDriver
 * https://www.guru99.com/take-screenshot-selenium-webdriver.html
 */
public class TakeScreenshotTest {

    private static Path outputDir = Paths.get(System.getProperty("user.dir"))
            .resolve("build/tmp/testOutput")
            .resolve(TakeScreenshotTest.class.getName());

    private static Store store;

    private WebDriver driver;

    @Test
    public void test_takeScreenshot() throws Exception {
        URL url = new URL("http://demo.guru99.com/V4/");
        driver.get(url.toString());
        // Convert WebDriver object to TakeScreenshot
        TakesScreenshot scrShot = (TakesScreenshot)driver;
        // take a screenshot image
        File imageFile = scrShot.getScreenshotAs(OutputType.FILE);
        // store the image into the store
        JobName jobName = new JobName("test_takeScreenshot");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        Metadata metadata =
                Metadata.builder(url).put("step", "1").build();
        store.write(jobName, jobTimestamp, FileType.PNG, metadata, imageFile);
        // compile a list of materials in the store
        MaterialList materialList =
                store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        Inspector inspector = Inspector.newInstance(store);
        String fileName = jobName + "-index.html";
        inspector.report(materialList, fileName);
    }

    @BeforeAll
    public static void beforeAll() {
        WebDriverManager.chromedriver().setup();
        // instantiate the store
        Path root = outputDir.resolve("store");
        store = Stores.newInstance(root);
    }

    @BeforeEach
    public void beforeEach() {
        driver = new ChromeDriver();
    }

    @AfterEach
    public void afterEach() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }


}

