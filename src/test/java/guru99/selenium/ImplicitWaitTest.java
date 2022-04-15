package guru99.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.concurrent.TimeUnit;

public class ImplicitWaitTest {

    @Test
    public void test_implicitlyWait() throws Exception {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        //
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
