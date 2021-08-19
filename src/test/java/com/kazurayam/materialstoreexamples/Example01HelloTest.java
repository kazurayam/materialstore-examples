package com.kazurayam.materialstoreexamples;

import com.kazurayam.materialstore.FileType;
import com.kazurayam.materialstore.JobName;
import com.kazurayam.materialstore.JobTimestamp;
import com.kazurayam.materialstore.Metadata;
import com.kazurayam.materialstore.Store;
import com.kazurayam.materialstore.Stores;
import com.kazurayam.subprocessj.Subprocess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Example01HelloTest {

    private static final String className =
            Example01HelloTest.class.getSimpleName();

    private static final Path root =
            Paths.get("./build/tmp/testOutput/" + className + "/store");

    private Store store;
    private JobName jobName;
    private JobTimestamp jobTimestamp;

    @BeforeAll
    public static void beforeAll() throws IOException {
        TestUtils.initDir(root);
    }

    @BeforeEach
    public void beforeEach() {
        store = Stores.newInstance(root);
        jobName = new JobName(className);
        jobTimestamp = JobTimestamp.now();
    }

    @Test
    public void test_write_a_text_file() {
        String content = "Hello, world!";
        store.write(jobName, jobTimestamp, FileType.TXT, Metadata.NULL_OBJECT,
                content, StandardCharsets.UTF_8);
    }

    @AfterEach
    public void after_each() throws IOException, InterruptedException {
        Subprocess subprocess = new Subprocess();
        subprocess.cwd(new File(root.toString()));
        Subprocess.CompletedProcess cp = subprocess.run(Arrays.asList("tree", "."));
        assert cp.returncode() == 0;
        Path tree = store.getRoot().resolve("tree.txt");
        Path index = store.getRoot().resolve("index.txt");
        TestUtils.writeLines(cp.stdout(), tree);
        TestUtils.copyIndex(store, jobName, jobTimestamp, index);
    }



}
