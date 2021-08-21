package com.kazurayam.materialstoreexamples;

import com.kazurayam.materialstore.FileType;
import com.kazurayam.materialstore.JobName;
import com.kazurayam.materialstore.JobTimestamp;
import com.kazurayam.materialstore.Material;
import com.kazurayam.materialstore.Metadata;
import com.kazurayam.materialstore.MetadataImpl;
import com.kazurayam.materialstore.MetadataPattern;
import com.kazurayam.materialstore.Store;
import com.kazurayam.materialstore.Stores;
import com.kazurayam.subprocessj.Subprocess;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Ex02StoringImagesTest {

    private static final String className =
            Ex02StoringImagesTest.class.getSimpleName();

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
    public void test_downloading_a_photo_from_web() throws Exception {
        URL url = new URL("https://kazurayam.github.io/materialstore-examples/Chapter01/images/umineko-1960x1960.jpg");
        byte[] bytes = TestUtils.downloadWebResourceAsByteArray(url);
        Metadata metadata = Metadata.builderWithUrl(url)
                .put("location", "Hachinohe,Aomori prefecture,Japan")
                .put("venue", "Kabushima")
                .put("title", "Umineko, a sea bird")
                .put("土地", "八戸市")
                .put("場所", "蕪島")
                .put("タイトル", "うみねこ")
                .build();
        store.write(jobName, jobTimestamp, FileType.JPEG, metadata, bytes);
    }

    @AfterEach
    public void afterEach() {}

    @AfterAll
    public static void afterAll() throws IOException, InterruptedException {
        List<Material> materialList = store.select(jobName, jobTimestamp, MetadataPattern.ANY);
        store.reportMaterials(jobName, materialList, "list.html");
        //
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
