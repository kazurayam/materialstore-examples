package com.kazurayam.materialstorebyexample.case0;

import com.google.common.collect.ImmutableMap;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MaterializingWebPageWithoutStore {

    private String urlLeft = "https://openweathermap.org/";   // London, GB as default city
    private String urlRight = "https://openweathermap.org/city/6077243";  // Montreal, CA


    public static void main(String[] args) throws Exception {
        String url = "https://www.google.com/";
        Map<String, String> query = ImmutableMap.of ("q", "cordova");

        // open the Chrome browser using Selenium WebDriver
        WebDriver driver = createChromeDriver();

    }



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

}
