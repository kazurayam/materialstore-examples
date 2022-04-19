package com.kazurayam.materialstore.tutorial.ch1;

import com.kazurayam.materialstore.Inspector;
import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Metadata;
import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.MProductGroupBuilder;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class VisualInspectionChronosModeTest extends VisualInspectionBase {

    private static Logger logger = LoggerFactory.getLogger(VisualInspectionChronosModeTest.class);

    @BeforeAll
    public static void beforeAll() throws Exception {
        // we use WebDriverManager to control the version of ChromDriver
        WebDriverManager.chromedriver().setup();
        // create a directory where this test will write output files
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        Path outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(VisualInspectionChronosModeTest.class.getName());
        Files.createDirectories(outputDir);
        // create a directory "store"
        Path root = outputDir.resolve("store");
        // prepare an instance of com.kazurayam.materialstore.filesystem.Store
        // which will control every writing/reading files withing the store
        store = Stores.newInstance(root);
        // specify names of sub-directories
        jobName = new JobName("MyAdminChronos");
    }

    @BeforeEach
    public void beforeEach() {
        // open Chrome browser
        driver = new ChromeDriver();
        // set the size of browser window
        driver.manage().window().setSize(new Dimension(1024, 768));
    }

    @Test
    public void test_chronos() throws Exception {
        String rightText = "http://devadmin.kazurayam.com,//img[@alt=\"umineko\"]";
        MaterialList currentMaterialList = materialize(rightText, store, jobName);
        //
        MProductGroup reduced = reduceChronos(store, currentMaterialList);
        //
        int warnings = report(store, reduced, 1.0D);
        assertEquals(0, warnings, "warnings=" + warnings);
    }

    /**
     *
     */
    MProductGroup reduceChronos(Store store,
                         MaterialList currentMaterialList)
            throws MaterialstoreException {
        logger.info("[reduce] store=" + store);
        logger.info("[reduce] currentMaterialList=" + currentMaterialList);
        assert currentMaterialList.size() > 0;

        MProductGroup prepared =
                MProductGroupBuilder.chronos(store, currentMaterialList);

        Inspector inspector = Inspector.newInstance(store);
        MProductGroup reduced = inspector.reduce(prepared);
        return reduced;
    }

    @AfterEach
    public void afterEach() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

}
