package guru99.selenium.screenshot;

import com.kazurayam.ashotwrapper.AShotWrapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class AShotTest {

    @Test
    public void test_takeScreenshot() throws Exception {
        driver.get("http://demo.guru99.com/V4/");
        this.takeSnapShot(driver, "./tmp/test_by_AShot.png");
    }

    void takeSnapShot(WebDriver webdriver, String filePath) throws Exception {
        AShotWrapper.Options options = new AShotWrapper.Options.Builder().build();
        // take a screenshot image
        BufferedImage image = AShotWrapper.takeEntirePageImage(webdriver, options);
        // save the image into file
        File destFile = new File(filePath);
        // copy the image into the specified destination
        ImageIO.write(image, "PNG", destFile);
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
