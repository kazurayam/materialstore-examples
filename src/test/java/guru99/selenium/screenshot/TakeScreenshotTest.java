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

/**
 * Guru99, How to Take Screenshot in Selenium WebDriver
 * https://www.guru99.com/take-screenshot-selenium-webdriver.html
 */
public class TakeScreenshotTest {

    @Test
    public void test_takeScreenshot() throws Exception {
        driver.get("http://demo.guru99.com/V4/");
        // Convert WebDriver object to TakeScreenshot
        TakesScreenshot scrShot = (TakesScreenshot)driver;
        // take a screenshot image
        File imageFile = scrShot.getScreenshotAs(OutputType.FILE);
        // copy the image into the specified destination
        File destFile = new File("./tmp/test.png");
        FileUtils.copyFile(imageFile, destFile);
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
