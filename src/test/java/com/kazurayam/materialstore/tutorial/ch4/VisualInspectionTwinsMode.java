package com.kazurayam.materialstore.tutorial.ch4;

import com.kazurayam.materialstore.Inspector;
import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.Stores;
import com.kazurayam.materialstore.reduce.MProductGroup;
import com.kazurayam.materialstore.reduce.MProductGroupBuilder;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class VisualInspectionTwinsMode extends VisualInspectionBase {

    private static Logger logger = LoggerFactory.getLogger(VisualInspectionTwinsMode.class);

    @BeforeAll
    public static void beforeAll() throws Exception {
        // we use WebDriverManager to control the version of ChromeDriver
        WebDriverManager.chromedriver().setup();
        // create a directory where this test will write output files
        Path projectDir = Paths.get(System.getProperty("user.dir"));
        Path outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(VisualInspectionTwinsMode.class.getName());
        Files.createDirectories(outputDir);
        // create a directory "store"
        Path root = outputDir.resolve("store");
        // prepare an instance of com.kazurayam.materialstore.filesystem.Store
        // which will control every writing/reading files withing the store
        store = Stores.newInstance(root);
        // specify names of sub-directories
        jobName = new JobName("MyAdminTwins");
    }

    @BeforeEach
    public void beforeEach() {
        // open Chrome browser
        driver = new ChromeDriver();
        // set the size of browser window
        driver.manage().window().setSize(new Dimension(1024, 768));
    }

    @Test
    public void test_twins() throws Exception {
        String leftText = "http://myadmin.kazurayam.com,//img[@alt=\"umineko\"]";
        MaterialList leftMaterialList = materialize(leftText, store, jobName);
        String rightText = "http://devadmin.kazurayam.com,//img[@alt=\"umineko\"]";
        MaterialList rightMaterialList = materialize(rightText, store, jobName);
        //
        MProductGroup reduced = reduceTwins(store, leftMaterialList, rightMaterialList);
        //
        int warnings = report(store, reduced, 1.0D);
        assertEquals(0, warnings, "warnings=" + warnings);
    }


    /**
     *
     */
    MProductGroup reduceTwins(Store store,
                         MaterialList left,
                         MaterialList right) throws MaterialstoreException {
        logger.info("[reduce] store=" + store);
        logger.info("[reduce] left=" + left);
        logger.info("[reduce] right=" + right);
        assert left.size() > 0;
        assert right.size() > 0;

        BiFunction<MaterialList, MaterialList, MProductGroup> func =
                (MaterialList leftML, MaterialList rightML) ->
                        MProductGroup.builder(leftML, rightML)
                                .ignoreKeys("URL.host", "URL.port", "URL.scheme")
                                .sort("step")
                                .build();
        MProductGroup prepared =
                MProductGroupBuilder.twins(store,
                        left, right, func);

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
