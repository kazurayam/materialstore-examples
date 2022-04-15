package guru99.selenium.wait;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

public class ExplicitWaitTest {

    @Test
    public void test_explicitlyWait() throws Exception {
        String eTitle = "Demo Guru99 Page";
        String aTitle = "";
        // launch Chrome and redirect it to the Base URL
        driver.get("http://demo.guru99.com/test/guru99home");
        // Maximizes the browser window
        driver.manage().window().maximize();
        // get the actual value of the title
        aTitle = driver.getTitle();
        // compare the actual title with the expected title
        if (aTitle.equals(eTitle)) {
            System.out.println("Test Passed");
        } else {
            System.out.println("Test Failed");
        }
        //
        WebDriverWait wait = new WebDriverWait(driver, 20);
        WebElement guru99seleniumlink =
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//a[contains(text(),'Testing')]")));
        guru99seleniumlink.click();
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
