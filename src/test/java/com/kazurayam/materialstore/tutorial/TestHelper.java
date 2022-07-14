package com.kazurayam.materialstore.tutorial;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class TestHelper {

    private final Logger logger = LoggerFactory.getLogger(TestHelper.class);

    public TestHelper() {}

    public static Path initializeOutputDir(Path projectDir, Class clazz) throws IOException {
        Path outputDir = projectDir.resolve("build/tmp/testOutput")
                .resolve(clazz.getName());
        if (Files.exists(outputDir)) {
            FileUtils.deleteDirectory(outputDir.toFile());
        }
        Files.createDirectories(outputDir);
        return outputDir;
    }

    public static WebDriver openHeadlessChrome() {
        // open Chrome browser
        ChromeOptions opt = new ChromeOptions();
        opt.addArguments("headless");
        WebDriver driver = new ChromeDriver(opt);
        // set the size of browser window
        Dimension dem = new Dimension(1024,768);
        driver.manage().window().setSize(dem);
        return driver;
    }

    public static void closeBrowser(WebDriver driver) {
        if (driver != null) {
            driver.quit();
        }
    }
}
