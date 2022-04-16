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
        // Chrome browser is opened by @BforeEach-annotated method

        // Navigate to the target URL
        driver.get("http://demo.guru99.com/V4/");

        // Convert WebDriver object to an instance of TakesScreenshot
        TakesScreenshot scrShot = (TakesScreenshot)driver;

        // take a screenshot in PNG format, which will be stored in a temporary file
        File imageFile = scrShot.getScreenshotAs(OutputType.FILE);

        // copy the image file into the specified destination
        File destFile = new File("./tmp/test.png");
        FileUtils.copyFile(imageFile, destFile);
    }

    @BeforeAll
    public static void beforeAll() {
        // we use https://bonigarcia.dev/webdrivermanager/ to control ChromeDriver
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void beforeEach() {
        // open Chrome browser
        driver = new ChromeDriver();
    }

    @AfterEach
    public void afterEach() {
        // close Chrome browser
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    WebDriver driver;
}
