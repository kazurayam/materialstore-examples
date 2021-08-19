package com.kazurayam.materialstoreexamples;

import com.google.common.collect.ImmutableMap;
import com.kazurayam.materialstore.DiffArtifacts;
import com.kazurayam.materialstore.FileType;
import com.kazurayam.materialstore.JobName;
import com.kazurayam.materialstore.JobTimestamp;
import com.kazurayam.materialstore.Material;
import com.kazurayam.materialstore.MetadataIgnoredKeys;
import com.kazurayam.materialstore.MetadataImpl;
import com.kazurayam.materialstore.MetadataPattern;
import com.kazurayam.materialstore.Store;
import com.kazurayam.materialstore.Stores;
import com.kazurayam.ashotwrapper.AShotWrapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisualTestingTwinsTest {

    private static final Path root = Paths.get("./build/tmp/testOutput/"
            + VisualTestingTwinsTest.class.getSimpleName()
            + "/store");

    private static Store store;
    private JobName jobName;
    private JobTimestamp jobTimestamp;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path dir = root;
        if (Files.exists(dir)) {
            // delete the directory to clear out using Java8 API
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectories(dir);
        //
        store = Stores.newInstance(root);
    }

    /**
     *
     */
    @Test
    void test_compare2EnvsOfWebApp() throws Exception {
        jobName = new JobName("test_compare2EnvsOfWebApp");
        jobTimestamp = JobTimestamp.now();

        // open the Chrome browser
        WebDriver driver = createChromeDriver();

        // visit the 1st page to take screenshot and save HTML source
        String profile1 = "ProductionEnv";
        WebInteractionResult result1 =
                doWebInteraction(driver, store, jobName, jobTimestamp,
                        profile1, new URL("http://myadmin.kazurayam.com/"));
        assertNotNull(result1);
        assertTrue(Files.exists(store.getPathOf(result1.entirePageScreenshot())));
        assertTrue(Files.exists(store.getPathOf(result1.webElementScreenshot())));
        assertTrue(Files.exists(store.getPathOf(result1.pageSource())));

        // visit the 2nd page to take screenshot and save HTML source
        String profile2 = "DevelopmentEnv";
        WebInteractionResult result2 =
                doWebInteraction(driver, store, jobName, jobTimestamp,
                        profile2, new URL("http://devadmin.kazurayam.com/"));
        assertNotNull(result2);
        assertTrue(Files.exists(store.getPathOf(result2.entirePageScreenshot())));
        assertTrue(Files.exists(store.getPathOf(result2.webElementScreenshot())));
        assertTrue(Files.exists(store.getPathOf(result2.pageSource())));

        // close the Chrome browser
        driver.quit();

        // pickup the materials that belong to each "profiles"
        List<Material> left = store.select(jobName, jobTimestamp,
                new MetadataPattern.Builder(ImmutableMap.of("profile", profile1)).build()
        );
        List<Material> right = store.select(jobName, jobTimestamp,
                new MetadataPattern.Builder(ImmutableMap.of("profile", profile2)).build()
        );

        // make DiffArtifacts object
        DiffArtifacts stuffedDiffArtifacts =
                store.makeDiff(left, right,
                         new MetadataIgnoredKeys.Builder()
                                 .ignoreKey("profile")
                                 .ignoreKey("URL.protocol")
                                 .ignoreKey("URL.host")
                                 .build());

        int countWarnings = stuffedDiffArtifacts.countWarnings(0.0d);
        //assertEquals(0, countWarnings);

        // compile a report where you can view the downloaded materials and the diffs
        Path file = store.reportDiffs(jobName, stuffedDiffArtifacts, 0.0d, "index.html");
        System.out.println("The report can be found at ${file.toString()}");
    }

    /**
     *
     * @param driver
     * @param store
     * @param jobName
     * @param jobTimestamp
     * @param profile
     * @param url
     * @return
     */
    private static WebInteractionResult doWebInteraction(WebDriver driver,
                                     Store store,
                                     JobName jobName,
                                     JobTimestamp jobTimestamp,
                                     String profile,
                                     URL url) {
        // visit the page
        driver.navigate().to(url.toString());

        // take and store the PNG screenshot of the entire page
        BufferedImage entirePageImage =
                AShotWrapper.takeEntirePageImage(driver);
        Material material1 = store.write(jobName, jobTimestamp, FileType.PNG,
                new MetadataImpl.Builder(url)
                        .put("category", "screenshot")
                        .put("profile", profile)
                        .put("xpath", "/html")
                        .build(),
                entirePageImage);
        assert material1 != null;

        // Waiting for an element to be present on the page.
        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                .ignoring(NoSuchElementException.class);
        // take and store the PNG screenshot of an HTML button element
        String xpath = "//img[@alt='umineko']";
        By byXpath = By.xpath(xpath);

        // explicitly wait for the <img> element is loaded in the page; it takes a few seconds to load the large picture
        WebElement imgElement = wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                return driver.findElement(byXpath);
            }
        });

        // take a screenshot of a specific HTML element
        BufferedImage elementImage =
                AShotWrapper.takeElementImage(driver, byXpath);
        Material material2 = store.write(jobName, jobTimestamp, FileType.PNG,
                new MetadataImpl.Builder(url)
                        .put("category", "screenshot")
                        .put("profile", profile)
                        .put("xpath", xpath)
                        .build(),
                elementImage);
        assert material2 != null;


        // get and store the HTML source of the page
        String html = tidyHtmlString(driver.getPageSource());
        Material material3 = store.write(jobName, jobTimestamp, FileType.HTML,
                new MetadataImpl.Builder(url)
                        .put("category", "page source")
                        .put("profile", profile)
                        .put("xpath", "/html")
                        .build(),
                html, StandardCharsets.UTF_8);
        assert material3 != null;

        return new WebInteractionResult(material1, material2, material3);
    }

    /**
     *
     */
    public static class WebInteractionResult {
        private final Material entirePageScreenshot;
        private final Material webElementScreenshot;
        private final Material pageSource;
        public WebInteractionResult(
                Material entirePageScreenshot,
                Material webElementScreenshot,
                Material pageSource) {
            this.entirePageScreenshot = entirePageScreenshot;
            this.webElementScreenshot = webElementScreenshot;
            this.pageSource = pageSource;
        }
        Material entirePageScreenshot() {
            return this.entirePageScreenshot;
        }
        Material webElementScreenshot() {
            return this.webElementScreenshot;
        }
        Material pageSource() {
            return this.pageSource;
        }
    }

    /**
     * Opens a headless Chrome Browser
     * @return WebDriver
     */
    static WebDriver createChromeDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(120, TimeUnit.MILLISECONDS);
        driver.manage().window().setSize(new Dimension(800, 800));
        return driver;
    }

    /**
     * return tidy HTML String using jsoup
     *
     * @param htmlString
     * @return tidy HTML string
     */
    static String tidyHtmlString(String htmlString) {
        Document doc = Jsoup.parse(htmlString);   // pretty print HTML
        return doc.toString();
    }

}
