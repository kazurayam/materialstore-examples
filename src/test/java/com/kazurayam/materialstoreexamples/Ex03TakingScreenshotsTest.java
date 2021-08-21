package com.kazurayam.materialstoreexamples;

import com.google.common.collect.ImmutableMap;
import com.kazurayam.ashotwrapper.AShotWrapper;
import com.kazurayam.materialstore.FileType;
import com.kazurayam.materialstore.JobName;
import com.kazurayam.materialstore.JobTimestamp;
import com.kazurayam.materialstore.Material;
import com.kazurayam.materialstore.MaterialList;
import com.kazurayam.materialstore.Metadata;
import com.kazurayam.materialstore.MetadataImpl;
import com.kazurayam.materialstore.MetadataPattern;
import com.kazurayam.materialstore.Store;
import com.kazurayam.materialstore.Stores;
import com.kazurayam.subprocessj.Subprocess;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Ex03TakingScreenshotsTest {

    private static final String className =
            Ex03TakingScreenshotsTest.class.getSimpleName();

    private static final Path root =
            Paths.get("./build/tmp/testOutput/" + className + "/store");

    private static Store store;
    private static JobName jobName;
    private static JobTimestamp jobTimestamp;
    private static WebDriver driver;

    @BeforeAll
    public static void beforeAll() throws IOException {
        TestUtils.initDir(root);
        store = Stores.newInstance(root);
        jobName = new JobName(className);
        jobTimestamp = JobTimestamp.now();
        // open the Chrome browser
        driver = TestUtils.createChromeDriver();
        // set the browser's window size
        driver.manage().window().setSize(new Dimension(1280,800));
    }

    @BeforeEach
    public void beforeEach() {
    }

    @Test
    public void takeScreenshot_OpenWeatherMap_city_Tokyo() throws Exception {
        URL url = new URL("https://openweathermap.org/city/1850147");
        File png = takeOpenWeatherMapPageScreenshot(url);
        Metadata metadata = Metadata.builderWithUrl(url)
                .put("country", "JP")
                .put("city", "Tokyo")
                .build();
        store.write(jobName, jobTimestamp, FileType.PNG, metadata, png);
    }

    @Test
    public void takeScreeenshot_OpenWeatherMap_city_HoChiMinh() throws Exception {
        URL url = new URL("https://openweathermap.org/city/1566083");
        File png = takeOpenWeatherMapPageScreenshot(url);
        Metadata metadata = MetadataImpl.builderWithUrl(url)
                .put("country", "VN")
                .put("city", "Thanh pho Ho Chi Minh")
                .build();
        store.write(jobName, jobTimestamp, FileType.PNG, metadata, png);
    }

    @Test
    public void takeScreenshot_OpenWeatherMap_city_Prague() throws Exception {
        URL url = new URL("https://openweathermap.org/city/3067696");
        File png = takeOpenWeatherMapPageScreenshot(url);
        Metadata metadata = Metadata.builderWithUrl(url)
                .put("country", "CZ")
                .put("city", "Prague")
                .build();
        store.write(jobName, jobTimestamp, FileType.PNG, metadata, png);
    }

    @Test
    public void takeScreenshot_OpenWeatherMap_city_Toronto() throws Exception {
        URL url = new URL("https://openweathermap.org/city/6167865");
        File png = takeOpenWeatherMapPageScreenshot(url);
        Metadata metadata = Metadata.builderWithUrl(url)
                .put("country", "CA")
                .put("city", "Toronto")
                .build();
        store.write(jobName, jobTimestamp, FileType.PNG, metadata, png);
    }

    static File takeOpenWeatherMapPageScreenshot(URL url) throws IOException, InterruptedException {
        // visit the URL in browser
        driver.navigate().to(url.toString());

        // wait for the page is fully loaded
        WebElement element = new WebDriverWait(driver, 10)
                    .until((WebDriver drv) -> drv.findElement(By.id("widget-map")));
        Thread.sleep(2000);  // to make sure that the Map is drawn in the canvas

        // create a temporary file to save the PNG image
        File file = Files.createTempFile("screenshot",".png").toFile();
        // take a PNG screenshot of the entire page, save it into a file
        AShotWrapper.saveEntirePageImage(driver, file);

        return file;
    }

    @AfterEach
    public void afterEach() {
    }

    @AfterAll
    public static void afterAll() throws IOException, InterruptedException {
        // close the browser
        driver.quit();

        // list all of 4 cities
        MaterialList materialList = store.select(jobName, jobTimestamp, MetadataPattern.ANY);
        store.reportMaterials(jobName, materialList, "fullList.html");

        // list 2 cities of which name stats with "To"
        MaterialList selected = store.select(jobName, jobTimestamp,
                MetadataPattern.builder().put("city", Pattern.compile("To.*")).build());
        store.reportMaterials(jobName, selected, "selected.html");

        //
        Subprocess subprocess = new Subprocess();
        subprocess.cwd(new File(root.toString()));
        Subprocess.CompletedProcess cp = subprocess.run(Arrays.asList("tree", "."));
        assert cp.returncode() == 0;
        Path tree = store.getRoot().resolve("tree.txt");
        Path index = store.getRoot().resolve("index.txt");
        TestUtils.writeLines(cp.stdout(), tree);
        TestUtils.copyIndex(store, jobName, jobTimestamp, index);
    }
}
