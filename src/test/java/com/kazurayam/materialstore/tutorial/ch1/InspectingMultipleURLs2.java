package com.kazurayam.materialstore.tutorial.ch1;

import com.kazurayam.materialstore.Inspector;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.Material;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.materialize.StorageDirectory;
import com.kazurayam.materialstore.materialize.Target;
import com.kazurayam.materialstore.materialize.TargetCSVReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.kazurayam.materialstore.materialize.MaterializingPageFunctions.storeEntirePageScreenshot;
import static com.kazurayam.materialstore.materialize.MaterializingPageFunctions.storeHTMLSource;

public class InspectingMultipleURLs2 {

    private static Store store;
    private JobName jobName;
    private JobTimestamp jobTimestamp;
    private WebDriver driver;
    private static Path targetCSV;

    @BeforeAll
    public static void beforeAll() throws Exception {
        // we use WebDriverManager to control the version of ChromeDriver
        WebDriverManager.chromedriver().setup();

        // create a directory where this test will write output files
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        Path outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(InspectingMultipleURLs2.class.getName());
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);

        // create a directory "store"
        Path root = outputDir.resolve("store");

        // prepare an instance of com.kazurayam.materialstore.filesystem.Store
        // which will control every writing/reading files within the store
        store = Stores.newInstance(root);

        // find the file which contains a list of target URL
        targetCSV =
                projectDir.resolve("src/test/resources/fixture")
                        .resolve("GithubRepos.csv");
        assert Files.exists(targetCSV);
    }

    @BeforeEach
    public void beforeEach() {
        // open Chrome browser
        ChromeOptions opt = new ChromeOptions();
        opt.addArguments("headless");
        driver = new ChromeDriver(opt);
        // set the size of browser window
        Dimension dem = new Dimension(1024,768);
        driver.manage().window().setSize(dem);
    }


    @Test
    public void test_multiple_URLs_using_builtin_functions() throws Exception {
        // specify names of sub-directories
        jobName = new JobName("test_multiple_URLs_using_builtin_functions");
        jobTimestamp = JobTimestamp.now();
        StorageDirectory sd = new StorageDirectory(store, jobName, jobTimestamp);

        // materialize the target URLs
        List<Target> targetList = TargetCSVReader.parse(targetCSV);
        int x = 1;
        for (Target t: targetList) {
            Target target = t.copyWith("seq", Integer.toString(x++));
            driver.navigate().to(target.getUrl());
            Material png = storeEntirePageScreenshot.accept(target, driver, sd);
            assert png != null;
            Material html = storeHTMLSource.accept(target, driver, sd);
            assert html != null;
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
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
