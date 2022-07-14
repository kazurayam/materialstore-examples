package com.kazurayam.materialstore.tutorial.ch3;

import com.kazurayam.materialstore.Inspector;
import com.kazurayam.materialstore.filesystem.FileType;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;

import com.kazurayam.materialstore.materialize.MaterializingPageFunction;
import com.kazurayam.materialstore.materialize.StorageDirectory;
import com.kazurayam.materialstore.materialize.Target;
import com.kazurayam.materialstore.materialize.TargetCSVReader;
import com.kazurayam.materialstore.tutorial.TestHelper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class InspectingMultipleURLs1 {

    private final Logger logger = LoggerFactory.getLogger(InspectingMultipleURLs1.class);
    private static Path projectDir;
    private static Store store;
    private WebDriver driver;

    @BeforeAll
    public static void beforeAll() throws Exception {
        // we use WebDriverManager to control the version of ChromeDriver
        WebDriverManager.chromedriver().setup();

        // create a directory where this test will write output files
        projectDir = Paths.get(System.getProperty("user.dir"));
        Path outputDir = TestHelper.initializeOutputDir(projectDir,
                InspectingMultipleURLs1.class);

        // create a directory "store"
        Path root = outputDir.resolve("store");

        // prepare an instance of com.kazurayam.materialstore.filesystem.Store
        // which will control every writing/reading files within the store
        store = Stores.newInstance(root);
    }

    @BeforeEach
    public void beforeEach() {
        driver = TestHelper.openHeadlessChrome();
    }



    @Test
    public void test_multiple_URLs_using_Functional_Interface() throws Exception {
        // find the file which contains a list of target URL
        Path targetCSV =
                projectDir.resolve("src/test/resources/fixture")
                        .resolve("weather.csv");
        assert Files.exists(targetCSV);
        // specify names of sub-directories
        JobName jobName = new JobName("test_multiple_URLs_using_Functional_Interface");
        JobTimestamp jobTimestamp = JobTimestamp.now();
        StorageDirectory storageDirectory = new StorageDirectory(store, jobName, jobTimestamp);

        // create a function to process the target
        MaterializingPageFunction<Target, WebDriver, StorageDirectory, Material> capture =
                (target, driver, sd) -> {
                    // make sure the page is loaded completely
                    WebDriverWait wait = new WebDriverWait(driver, 20);
                    WebElement handle =
                            wait.until(ExpectedConditions.visibilityOfElementLocated(
                                    target.getBy()));
                    assert handle != null;
                    // take the screenshot of the page using Selenium
                    TakesScreenshot shooter = ((TakesScreenshot)driver);
                    File tempFile = shooter.getScreenshotAs(OutputType.FILE);
                    // copy the image into the store
                    Metadata metadata = Metadata.builder(target.getUrl()).build();
                    return store.write(sd.getJobName(), sd.getJobTimestamp(),
                            FileType.PNG, metadata, tempFile);
                };

        // materialize the target URLs
        List<Target> targetList = TargetCSVReader.parse(targetCSV);
        int x = 1;
        for (Target t : targetList) {
            logger.info("processing " + t.getUrl());
            Target target = t.copyWith("seq", Integer.toString(x++));
            driver.navigate().to(target.getUrl());
            // call the function defined above
            capture.accept(target, driver, storageDirectory);
        }

        // compile the HTML report
        MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
        // compile an HTML report of the materials
        Inspector inspector = Inspector.newInstance(store);
        String fileName = jobName.toString() + "-list.html";
        Path report = inspector.report(materialList, fileName);
        System.out.println("The report will be found at " + report.toString());
    }



    @AfterEach
    public void afterEach() {
        TestHelper.closeBrowser(driver);
    }
}
