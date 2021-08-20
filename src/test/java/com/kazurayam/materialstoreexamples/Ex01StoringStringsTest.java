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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Ex01StoringStringsTest {

    private static final String className =
            Ex01StoringStringsTest.class.getSimpleName();

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
    public void test_write_a_text_file() throws Exception {
        String content = String.join("\n",
                "May the road rise to meet you",
                "May the wind be always at your back",
                "May the sun shine warm upon your face",
                "The rains fall soft upon your fields",
                "",
                "And until we meet again, until we meet again",
                "May God hold you in the palm of his hand",
                "And until we meet again, until we meet again",
                "May God hold you in the palm of his hand");

        Metadata metadata = new MetadataImpl.Builder(
                new URL("https://youtu.be/VF-ZH4mymtk"))
                .put("title", "An Irish Blessing")
                .build();

        store.write(jobName, jobTimestamp, FileType.TXT, metadata,
                content, StandardCharsets.UTF_8);
    }

    @Test
    public void test_write_one_more_text_file() throws IOException {
        String content = String.join("\n",
                "Somewhere over the rainbow way up high",
                "There's a land that I heard of once in a lullaby",
                "Somewhere over the rainbow skies are blue",
                "And the dreams that you dare to dream really do come true");

        Metadata metadata = new MetadataImpl.Builder(
                new URL("https://www.youtube.com/watch?v=PSZxmZmBfnU"))
                .put("title", "Over the rainbow")
                .build();

        store.write(jobName, jobTimestamp, FileType.TXT, metadata,
                content, StandardCharsets.UTF_8);
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
