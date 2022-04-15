package guru99.selenium.screenshot;

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

public class TakeScreenshotTest {

    @Test
    public void test_takeScreenshot() throws Exception {
        driver.get("http://demo.guru99.com/V4/");
        this.takeSnapShot(driver, "./tmp/test.png");
    }

    void takeSnapShot(WebDriver webdriver, String filePath) throws Exception {
        // Convert WebDriver object to TakeScreenshot
        TakesScreenshot scrShot = (TakesScreenshot)webdriver;
        // take a screenshot image
        File srcFile = scrShot.getScreenshotAs(OutputType.FILE);
        File destFile = new File(filePath);
        // copy the image into the specified destination
        FileUtils.copyFile(srcFile, destFile);
    }

    @BeforeAll
    public static void beforeAll() {
        WebDriverManager.chromedriver().setup();
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

    WebDriver driver;
}
