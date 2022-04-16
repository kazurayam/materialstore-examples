-   [materialstore tutorial](#materialstore-tutorial)
    -   [Links](#links)
        -   [Repository and product](#repository-and-product)
        -   [API documents](#api-documents)
    -   [Introduction](#introduction)
        -   [Background](#background)
        -   [Problem to solve](#problem-to-solve)
            -   [Problem1 Shouldn’t repeat inventing directory structures](#problem1-shouldnt-repeat-inventing-directory-structures)
            -   [Problem2 Metadata of Web page disappeared](#problem2-metadata-of-web-page-disappeared)
            -   [Problem3 Shouldn’t repeat writing code for reporting](#problem3-shouldnt-repeat-writing-code-for-reporting)
            -   [Problem4 Want to perform Visual Inspection](#problem4-want-to-perform-visual-inspection)
        -   [Solution by Materialstore](#solution-by-materialstore)
            -   [Terminology](#terminology)
            -   [Sample code](#sample-code)
            -   [Output directory structure](#output-directory-structure)
            -   [The index file](#the-index-file)
            -   [Report generated](#report-generated)

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

Based on the sample code above, I wrote many Selenium tests that take bunches of screenshots.
During the course, I found a few problems in the code shown above.

#### Problem1 Shouldn’t repeat inventing directory structures

The Selenium library supports taking a screenshot of browser window
and saving image into a temporary file.
However, Selenium does not provide a mean of organizing the created files.
I had to write codes that create a directory structure to store the PNG files.
I ran a single test many times, and I wanted to preserve the outcomes
of 1st, 2nd and 3rd run.
So I need to create directories with name of timestamp format like
`20220414_093417`, `20220415_163924`, `20220416_170836` and so on.
I repeated inventing directory trees to store PNG files.
It was just tiring and boring.

The [DRY](https://en.wikipedia.org/wiki/Don%27t_repeat_yourself) principle came up to me.
I wanted to invent a reusable library that manages a directory tree to
store screenshots created by my Selenium tests.

#### Problem2 Metadata of Web page disappeared

By executing the test, I got a file `./tmp/test.png`.
In fact the file was created out of a web page at the URL `http://demo.guru99.com/V4/`.
But the metadata (from which URL it was created,
at which stage of test processing it was created, etc)
is not recorded in the stored file.
Other program may read the file to reuse somehow, but it will never
be informed of the metadata that the `./tmp/test.png` file
was created out of the URL `http://demo.guru99.com/V4/`.

Without the metadata, screenshots are not reusable for any purposes.
Screenshots become garbage as soon as created.

#### Problem3 Shouldn’t repeat writing code for reporting

When I got many PNG files on disk,
naturally I wanted a easy method to view images.
I wrote a code to generate an HTML report of PNG files.
I realised I should make the code as a reusable library.

#### Problem4 Want to perform Visual Inspection

I developed a set of tests that take screenshots of web pages of
a single web app with 100% coverage.
Then I wanted to perform **Visual Inspection**:
compare the Development environment vs the Production environment;
compare the pages Before vs After a software update.

I may find unexpected visual differences between the two sets.
The visual inspection report will let me know exactly
which part of my web application needs to be looked at.

However, a program that compares 2 sets of screenshots was difficult to implement.
It required the problem 1, 2 and 3 to be resolved as prerequisite.

### Solution by Materialstore

#### Terminology

In this document I use a special term "**material**".
A material is a file of which content is downloaded from a URL.
A screenshot of a web page is a typical material.
An HTML source text of a web page can be a material as well.
Any file downloaded from web can be a material
--- `.png`, `.jpg`, `.html`, `.json`, `.xml`, `.txt`, `.csv`, `.js`, `.css`, `.xlsx`, `.pdf` and so on.

#### Sample code

The following code is a JUnit5-based test written in Java.
It performs the following processing:

1.  it `https://www.google.com`

2.  in the `<input type="text" name="q">` field, type a query string

3.  take a screenshot and save the PNG image into the `store` directory.

4.  and push SEND key; wait for the response

5.  once the Search Result page is shown, take another screenshot and save it into the `store` directory

6.  compile an HTML report that renders the 2 materials (screenshots in PNG).

7.  the report will show metadata of the materials, such as URL of web pages.

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
    import org.openqa.selenium.Dimension;
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
            // we use WebDriverManager to control the version of ChromeDriver
            WebDriverManager.chromedriver().setup();

            // create a directory where this test will write output files
            Path projectDir = Paths.get(System.getProperty("user.dir"));
            outputDir = projectDir.resolve("build/tmp/testOutput")
                    .resolve(GoogleSearchTest.class.getName());
            Files.createDirectories(outputDir);

            // create a directory "store"
            Path root = outputDir.resolve("store");

            // prepare an instance of com.kazurayam.materialstore.filesystem.Store
            // which will control every writing/reading files within the store
            store = Stores.newInstance(root);
        }

        @BeforeEach
        public void beforeEach() {
            // specify names of sub-directories
            jobName = new JobName("GoogleSearch");
            jobTimestamp = JobTimestamp.now();

            // open Chrome browser
            driver = new ChromeDriver();
            // set the size of browser window
            Dimension dem = new Dimension(1024,768);
            driver.manage().window().setSize(dem);
        }

        @Test
        public void test_google_search() throws Exception {
            WebDriverWait wait = new WebDriverWait(driver, 10);

            // let Chrome navigate to the Google Search page
            URL searchPage = new URL("https://www.google.com");
            driver.navigate().to(searchPage);

            // type a query string into the <input type="text" name="q"> field
            By by_input_q = By.cssSelector("input[name=\"q\"]");
            wait.until(ExpectedConditions.visibilityOfElementLocated(by_input_q));
            WebElement we_input_q = driver.findElement(by_input_q);
            String qValue = "Shohei Ohtani";
            we_input_q.sendKeys(qValue);

            // take screenshot of the Google Search page
            TakesScreenshot scrShot = (TakesScreenshot) driver;
            File tempFile1 = scrShot.getScreenshotAs(OutputType.FILE);

            // store the screenshot into the store
            Metadata metadata =
                    Metadata.builder(searchPage)
                            .put("step", "1")    // remember the step identification
                            .put("q", qValue)    // remember the query string typed
                            .build();
            store.write(jobName, jobTimestamp, FileType.PNG, metadata, tempFile1);

            // send ENTER to execute a search request;
            // then the browser will navigate to the Search Result page
            we_input_q.sendKeys(Keys.chord(Keys.ENTER));

            // wait for the Search Result page to load completely
            By by_img_logo = By.xpath("//div[contains(@class,'logo')]/a/img");
            wait.until(ExpectedConditions.visibilityOfElementLocated(by_img_logo));

            // take a screenshot of the Search Result page
            File tempFile2 = scrShot.getScreenshotAs(OutputType.FILE);

            // save the image into the store
            URL resultPageURL = new URL(driver.getCurrentUrl());
            Metadata metadata2 =
                    Metadata.builder(resultPageURL)
                            .put("step", "2")   // this is the 2nd step
                            .build();
            store.write(jobName, jobTimestamp, FileType.PNG, metadata2, tempFile2);

            // Now I want to compile a report in HTML.
            // get the list of all materials stored in the "store/<jobName>/<jobTimestamp>" directory
            MaterialList materialList = store.select(jobName, jobTimestamp, QueryOnMetadata.ANY);

            // compile an HTML report of the materials
            Inspector inspector = Inspector.newInstance(store);
            String fileName = jobName.toString() + "-list.html";
            Path report = inspector.report(materialList, fileName);
            System.out.println("The report will be found at " + report.toString());
        }

        @AfterEach
        public void afterEach() {
            if (driver != null) {
                driver.quit();
                driver = null;
            }
        }
    }

#### Output directory structure

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

#### The index file

The `store/GoogleSearch/yyyyMMdd_hhmmss/index` file would be interesting.
Its content is something like this:

    6dd1994bc0d92ba8e040cd38bc3d19c8af78dac6 png {"URL.host":"www.google.com", "URL.path":"", "URL.port":"80", "URL.protocol":"https", "q":"Shohei Ohtani", "step":"1"}
    b54640d1e106e07567413b5a712f8da824577612    png {"URL.host":"www.google.com", "URL.path":"/search", "URL.port":"80", "URL.protocol":"https", "URL.query":"q=Shohei+Ohtani&source=hp&ei=xHdZYuqMAbS32roPrJug8A4&iflsig=AHkkrS4AAAAAYlmF1GUo__T66C-cQAHefKOwYWAzgZva&ved=0ahUKEwjq_urdmpb3AhW0m1YBHawNCO4Q4dUDCAk&uact=5&oq=Shohei+Ohtani&gs_lcp=Cgdnd3Mtd2l6EAMyCwgAEIAEELEDEIMBMgsIABCABBCxAxCDATIFCAAQgAQyBQgAEIAEMgUIABCABDIFCAAQgAQyBQgAEIAEMgUIABCABFAAWCZg5QNoAHAAeACAAY0BiAHmA5IBAzEuM5gBAKABAQ&sclient=gws-wiz", "step":"2"}

Points to note:

1.  The `index` file contains a sequence of lines. Each line comprises with 3 parts:
    *ID, \_FileType* and *Metadata*. The 3 parts are delimited by tabs. The `index` file
    is encoded with UTF-8.

2.  A single line in the `index` file corresponds to a single physical file
    in the `objects` subdirectory.

3.  The name of physical files in the `objects` subdirectory has a standard format,
    that is 40 characters of hex-decimal string followed by a dot "." and ends with a FileType extension.

4.  The 40 characters (`ID` for short) are the SHA1 digital signature
    derived from the content byte array of each file.

5.  In the Materialstore world, you (a programmer or a tester) are no longer responsible for naming each physical files.

6.  Each line in `index` file contains the **Metadata**: the URL out of which screenshots are taken, and the fact that I made a query for "Shohei Ohtani" to Google.

7.  What type of data can I put in the **Metadata**? --- quite flexible.
    You can put any pair of Strings. The API supports a shortcut method to
    add a URL into Metadata because URL is most frequently used as Metadata.

8.  The **Metadata** of each line in `index` MUST be unique in a `index` file.
    An attempt to write an object into the store with
    duplicating **Metadata** with already stored object will be fail.
    You, programmer/tester, are asked to assign descriptive enough
    **Metadata** to each object.

9.  The `com.kazurayam.materialstore.filesystem.Store` class implements
    `write` methods to store files into the store,
    and `select` methods to retrieve materials out of the store.
    See the javadoc of [Store](https://kazurayam.github.io/materialstore/api/).

#### Report generated

The test generated a HTML like this:

-   [testOutput/com.kazurayam.materialstore.tutorial.ch1.GoogleSearchTest/store/GoogleSearch-list.html](testOutput/com.kazurayam.materialstore.tutorial.ch1.GoogleSearchTest/store/GoogleSearch-list.html)

![GoogleSearch html](images/ch1/GoogleSearch-html.png)

In this section, I showed a sample code that demonstrates how
the materialstore library resolves the Problem1 (directory structure),
Problem2 (Metadata) and Problem3 (Report).
The Problem4 (Visual Inspection) is a lot more complex stuff.
I will discuss Visual Inspection later.
