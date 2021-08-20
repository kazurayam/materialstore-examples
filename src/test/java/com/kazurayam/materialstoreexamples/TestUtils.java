package com.kazurayam.materialstoreexamples;

import com.kazurayam.ashotwrapper.AShotWrapper;
import com.kazurayam.materialstore.JobName;
import com.kazurayam.materialstore.JobTimestamp;
import com.kazurayam.materialstore.Store;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class TestUtils {

    protected TestUtils() {}

    /**
     * Will delete the dir if it is present, will delete its contents recursively,
     * and recreate the empty dir
     * @param dir
     * @throws IOException
     */
    static void initDir(Path dir) throws IOException {
        if (Files.exists(dir)) {
            // delete the directory to clear out using Java8 API
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectories(dir);
    }

    static void writeLines(List<String> lines, Path outFile) throws IOException {
        PrintWriter pr = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(outFile.toFile()), "UTF-8")));
        lines.forEach(line -> {
            pr.println(line);
        });
        pr.flush();
        pr.close();
    }

    static void copyIndex(Store store, JobName jobName, JobTimestamp jobTimestamp, Path outFile)
            throws IOException {
        Path index = store.getRoot().resolve(jobName.toString())
                .resolve(jobTimestamp.toString()).resolve("index");
        Files.copy(index, outFile);
    }

    static byte[] downloadWebResourceAsByteArray(URL url) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (InputStream inputStream = url.openStream()) {
            int n = 0;
            byte [] buffer = new byte[ 1024 ];
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
        }
        return output.toByteArray();
    }

    /**
     * Opens a headless Chrome Browser
     * @return WebDriver
     */
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
