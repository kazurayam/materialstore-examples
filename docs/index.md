-   [materialstore tutorial](#materialstore-tutorial)
    -   [Links](#links)
        -   [Repository and product](#repository-and-product)
        -   [API documents](#api-documents)
    -   [Introduction](#introduction)
        -   [Background](#background)
        -   [Problem to solve](#problem-to-solve)
        -   [Solution by Materialstore](#solution-by-materialstore)

# materialstore tutorial

## Links

### Repository and product

-   [materialstore on GitHub](https://github.com/kazurayam/materialstore)

-   [Maven Central URL](https://mvnrepository.com/artifact/com.kazurayam/materialstore)

### API documents

-   [materialstore Javadoc](https://kazurayam.github.io/materialstore/api/)

-   [materialstore-mapper Javadoc](https://kazurayam.github.io/materialstore-mapper/api/)

## Introduction

### Background

Several years ago when I worked for an IT company, I endeavored to develop
automated UI tests for their Web applications.
I studied Selenium WebDriver in Java.
I studied the following article to learn how to take screenshots of web pages.

-   [Guru99, How to Take Screenshot in Selenium WebDriver](https://www.guru99.com/take-screenshot-selenium-webdriver.html)

I retyped their sample code. Here I will quote the entire source codes.

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

When I ran this, the test produced a PNG image file in the `tmp` directory
under the project’s root directory:

    :~/github/materialstore-tutorial (master *+)
    $ tree ./tmp
    ./tmp
    └── test.png

    0 directories, 1 file

The `test.png` image looked as this:

![test](https://kazurayam.github.io/materialstore-tutorial/images/ch1/test.png)

The code worked just fine.

### Problem to solve

I wrote many Selenium tests that take bunches of screenshots.
During the course, I found several problems in the code shown above.

#### (1) I had to repeat writing code to create directories to store files

The Selenium library supports taking a screenshot into a temporary file.
But Selenium does not provide a mean of organizing the created files.
I had to repeatedly write codes that create a directory structure to store the PNG files.

I tend to run a single test many times. If I want to reserve the outcomes of 1st, 2nd and 3rd execution,
I need to create directories with name of timestamp format like
`20220414_093417`, `20220415_163924`, `20220416_170836` and so on.
I repeated writing such code to create this directory structure.
It was just boring.
I remembered the "Don’t Repeat Yourself" principle.

#### (2) Metadata of Web pages disappear

By executing the test, I got a file `./tmp/test.png`.
In fact the file was created out of a web page at the URL `http://demo.guru99.com/V4/`.
But that **Metadata** (from which URL it was created,
at which stage of test processing it was created,
with what input data from human, etc) is not recorded
in the stored image file.
Other programs will never be informed of the metadata that
the `./tmp/test.png` file was created
out of the URL `http://demo.guru99.com/V4/`.
Without the metadata, screenshots are not reusable for any purposes.
The screenshots become bulky garbages as soon as created.

#### (3) I had to repeat writing code to report the List of stored files

When I get many PNG files on disk,
naturally I want to have a method of viewing them easily.
I wrote a code to generate an HTML report of PNG files.
I repeatedly need the report for many other cases.
I realised I should make the code as a reusable library.

#### (4) I wanted to compare 2 sets of screenshots of a single Web app

I developed a set of tests that take screenshots with 100% coverage
of page types of a single web app.
Then I wanted to perform Visual Inspection: compare the Development env vs the Production env,
or a system Before vs After updating software version.

If I find any visual differences between the two sets,
that becomes the best checklist for me to improve the software quality.
The visual differences could drive my development works productively.

### Solution by Materialstore

#### test code

The following Java8 program ia s JUnit8 test, which performs the following:

1.  it opens Chrome browser, navigates to `https://www.google.com`

2.  in the `<input type="text" name="q">` field, type a query string, take a screenshot and save the PNG image into the `store` directory.

3.  and do SEND; wait for the respnse; take anothr screenshot and save it into the `store` directory

4.  compile a HTML report that renders the 2 PNG files with the metadata (URL etc)

<!-- -->

    package com.kazurayam.materialstore.tutorial.ch1;

    import com.kazurayam.materialstore.Inspector;
    import com.kazurayam.materialstore.filesystem.FileType;
    import com.kazurayam.materialstore.filesystem.JobName;
    import com.kazurayam.materialstore.filesystem.JobTimestamp;
    import com.kazurayam.materialstore.filesystem.MaterialList;
    import com.kazurayam.materialstore.filesystem.Metadata;
    import com.kazurayam.materialstore.filesystem.QueryOnMetadata;
    import com.kazurayam.materialstore.filesystem.Store;
    import com.kazurayam.materialstore.filesystem.Stores;
    import io.github.bonigarcia.wdm.WebDriverManager;
    import org.junit.jupiter.api.AfterEach;
    import org.junit.jupiter.api.BeforeAll;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.openqa.selenium.By;
    import org.openqa.selenium.Keys;
    import org.openqa.selenium.OutputType;
    import org.openqa.selenium.TakesScreenshot;
    import org.openqa.selenium.WebDriver;
    import org.openqa.selenium.WebElement;
    import org.openqa.selenium.chrome.ChromeDriver;
    import org.openqa.selenium.support.ui.ExpectedConditions;
    import org.openqa.selenium.support.ui.WebDriverWait;

    import java.io.File;
    import java.net.URL;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;

    /**
     * A Selenium Test that does the following:
     * 1. open Chrome browser
     * 2. visit "https://www.google.com/
     * 3. make a search for "Shohei Ohtani"
     * 4. take screenshots of the screen using AShot
     * 5. download HTML source of the web pages pages
     * 6. store the PNG and HTML files into the materialstore
     */
    public class GoogleSearchTest {

        private static Path outputDir;
        private static Store store;
        private JobName jobName;
        private JobTimestamp jobTimestamp;
        private WebDriver driver;

        @BeforeAll
        public static void beforeAll() throws Exception {
            Path projectDir = Paths.get(System.getProperty("user.dir"));
            outputDir = projectDir.resolve("build/tmp/testOutput")
                    .resolve(GoogleSearchTest.class.getName());
            Files.createDirectories(outputDir);
            WebDriverManager.chromedriver().setup();
            Path root = outputDir.resolve("store");
            store = Stores.newInstance(root);
        }

        @BeforeEach
        public void beforeEach() {
            jobName = new JobName("GoogleSearch");
            jobTimestamp = JobTimestamp.now();
            driver = new ChromeDriver();
        }

        @Test
        public void test_google_search() throws Exception {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            // open the Google Search page
            URL entryURL = new URL("https://www.google.com");
            driver.navigate().to(entryURL);
            // set a query into the <input name="q">
            By by_input_q = By.cssSelector("input[name=\"q\"]");
            wait.until(ExpectedConditions.visibilityOfElementLocated(by_input_q));
            WebElement we_input_q = driver.findElement(by_input_q);
            String qValue = "Shohei Ohtani";
            we_input_q.sendKeys(qValue);
            // take the screenshot of the Google Search page,
            TakesScreenshot scrShot = (TakesScreenshot) driver;
            File srcFile1 = scrShot.getScreenshotAs(OutputType.FILE);
            // save the image into the store
            Metadata metadata =
                    Metadata.builder(entryURL)
                            .put("step", "1")
                            .put("q", qValue)
                            .build();
            store.write(jobName, jobTimestamp, FileType.PNG, metadata, srcFile1);
            // send ENTER to execute a search request
            we_input_q.sendKeys(Keys.chord(Keys.ENTER));
            // wait for the search result page to load
            By by_img_logo = By.xpath("//div[contains(@class,'logo')]/a/img");
            wait.until(ExpectedConditions.visibilityOfElementLocated(by_img_logo));
            // take screenshot of the result page, store the image into the store
            File srcFile2 = scrShot.getScreenshotAs(OutputType.FILE);
            // save the image into the store
            URL resultPageURL = new URL(driver.getCurrentUrl());
            Metadata metadata2 = Metadata.builder(resultPageURL).put("step", "2").build();
            store.write(jobName, jobTimestamp, FileType.PNG, metadata2, srcFile2);
            // get the MaterialList
            MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);
            // compile the report
            Inspector inspector = Inspector.newInstance(store);
            String fileName = jobName.toString() + "-list.html";
            Path report = inspector.report(materialList, fileName);
            System.out.println("report found at " + report.toString());
        }

        @AfterEach
        public void afterEach() {
            if (driver != null) {
                driver.quit();
                driver = null;
            }
        }
    }

When I ran the test, it creates a directory named `store` under the
project’s directory where a tree of directories/files are created.

    :~/github/materialstore-tutorial (master *+)
    $ tree ./build/tmp/testOutput
    ./build/tmp/testOutput
    └── com.kazurayam.materialstore.tutorial.ch1.GoogleSearchTest
        └── store
            ├── GoogleSearch
            │   └── 20220415_224026
            │       ├── index
            │       └── objects
            │           ├── a7a5c13181ccadc1502bd325df2fa43d6a58a5be.png
            │           └── fe165e0a3a4577caa0c5756a526b3fc320e0b64e.png
            └── GoogleSearch-list.html

    5 directories, 4 files

The `store/GoogleSearch/yyyyMMdd_hhmmss/index` file would be interesting.
Its content is something like this:

    6dd1994bc0d92ba8e040cd38bc3d19c8af78dac6 png {"URL.host":"www.google.com", "URL.path":"", "URL.port":"80", "URL.protocol":"https", "q":"Shohei Ohtani", "step":"1"}
    b54640d1e106e07567413b5a712f8da824577612    png {"URL.host":"www.google.com", "URL.path":"/search", "URL.port":"80", "URL.protocol":"https", "URL.query":"q=Shohei+Ohtani&source=hp&ei=xHdZYuqMAbS32roPrJug8A4&iflsig=AHkkrS4AAAAAYlmF1GUo__T66C-cQAHefKOwYWAzgZva&ved=0ahUKEwjq_urdmpb3AhW0m1YBHawNCO4Q4dUDCAk&uact=5&oq=Shohei+Ohtani&gs_lcp=Cgdnd3Mtd2l6EAMyCwgAEIAEELEDEIMBMgsIABCABBCxAxCDATIFCAAQgAQyBQgAEIAEMgUIABCABDIFCAAQgAQyBQgAEIAEMgUIABCABFAAWCZg5QNoAHAAeACAAY0BiAHmA5IBAzEuM5gBAKABAQ&sclient=gws-wiz", "step":"2"}

Please note that the `index` file contains the **Metadata**: the URL out of which
screenshots are taken, and the fact that I made a query for "Shohei Ohtani" to Google.

The test generated a HTML
