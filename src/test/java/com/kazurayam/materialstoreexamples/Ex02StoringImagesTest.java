package com.kazurayam.materialstoreexamples;

import com.kazurayam.materialstore.FileType;
import com.kazurayam.materialstore.JobName;
import com.kazurayam.materialstore.JobTimestamp;
import com.kazurayam.materialstore.Metadata;
import com.kazurayam.materialstore.MetadataImpl;
import com.kazurayam.materialstore.Store;
import com.kazurayam.materialstore.Stores;
import com.kazurayam.subprocessj.Subprocess;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Ex02StoringImages {

    private static final String className =
            Ex01StoringStrings.class.getSimpleName();

    private static final Path root =
            Paths.get("./build/tmp/testOutput/" + className + "/store");

    private static Store store;
    private static JobName jobName;
    private static JobTimestamp jobTimestamp;

    @BeforeAll
    public static void beforeAll() throws IOException {
        TestUtils.initDir(root);
        store = Stores.newInstance(root);
        jobName = new JobName(className);
        jobTimestamp = JobTimestamp.now();
    }

    @BeforeEach
    public void beforeEach() {}

    @Test
    public void test_storing_a_photo() throws Exception {
        URL url = new URL("https://photo53.com/img/fushimiinari27.jpg");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (InputStream inputStream = url.openStream()) {
            int n = 0;
            byte [] buffer = new byte[ 1024 ];
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
        }
        byte[] bytes = output.toByteArray();

        Metadata metadata = new MetadataImpl.Builder(url)
                .put("location", "京都 伏見稲荷")
                .put("title", "千本鳥居")
                .build();

        store.write(jobName, jobTimestamp, FileType.TXT, metadata, bytes);
    }

    @AfterEach
    public void afterEach() {}

    @AfterAll
    public static void afterAll() throws IOException, InterruptedException {
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
