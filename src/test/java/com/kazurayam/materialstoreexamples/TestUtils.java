package com.kazurayam.materialstoreexamples;

import com.kazurayam.materialstore.JobName;
import com.kazurayam.materialstore.JobTimestamp;
import com.kazurayam.materialstore.Store;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

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
}
