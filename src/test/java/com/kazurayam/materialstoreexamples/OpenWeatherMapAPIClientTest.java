package com.kazurayam.materialstoreexamples;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.kazurayam.materialstore.FileType;
import com.kazurayam.materialstore.JobName;
import com.kazurayam.materialstore.JobTimestamp;
import com.kazurayam.materialstore.Material;
import com.kazurayam.materialstore.Metadata;
import com.kazurayam.materialstore.MetadataImpl;
import com.kazurayam.materialstore.MetadataPattern;
import com.kazurayam.materialstore.Store;
import com.kazurayam.materialstore.Stores;
import com.kazurayam.materialstore.reporter.MaterialsBasicReporter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests com.kazurayam.materialstoreexamples.OpenWeatherMapClient class.
 * Runs that class to obtain a JSON text.
 * Stores the JSON text into local disk using com.kazurayam.materialstore.Store API.
 */
public class OpenWeatherMapAPIClientTest {

    private static final Path materials =
            Paths.get("./build/tmp/testOutput/"
                    + OpenWeatherMapAPIClientTest.class.getSimpleName()
                    + "/store");

    private OpenWeatherMapClient client;

    private static Store store;
    private static JobName jobName;
    private static JobTimestamp jobTimestamp;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path dir = materials;
        if (Files.exists(dir)) {
            // delete the directory to clear out using Java8 API
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectories(dir);
        //
        store = Stores.newInstance(materials);
        jobName = new JobName("OpenWeatherMapClientTest");
        jobTimestamp = JobTimestamp.now();
    }

    @BeforeEach
    public void beforeEach() {
        client = new OpenWeatherMapClient();
    }

    /**
     * get the weather forecast JSON of Saint Petersburg,ru from OpenWeatherMap
     *
     * @throws Exception
     */
    @Test
    public void test_id() throws Exception {
        // param to pass to the OpenWeatherMap service
        Map<String, String> param = ImmutableMap.of("id", "498817");

        // download JSON
        String compactJson = client.getOpenWeatherData(param);
        assertNotNull(compactJson);
        assertTrue(compactJson.length() > 0);

        // store the JSON file into the store
        Metadata metadata = new MetadataImpl.Builder(param).build();
        store.write(jobName, jobTimestamp, FileType.JSON, metadata,
                prettyPrintJsonString(compactJson));
    }

    /**
     * get the weather forecast JSON of Hachinohe,jp from OpenWeatherMap
     */
    @Test
    public void test_q() throws Exception {
        Map<String, String> param = ImmutableMap.of("q", "Hachinohe");

        // download JSON
        String compactJson = client.getOpenWeatherData(param);
        assertNotNull(compactJson);
        assertTrue(compactJson.length() > 0);

        // store the JSON file into the store
        Metadata metadata = new MetadataImpl.Builder(param).build();
        store.write(jobName, jobTimestamp, FileType.JSON, metadata,
                prettyPrintJsonString(compactJson));
    }


    /**
     * create a report in HTML format
     * where you can view 2 JSON files stored by the 2 test cases.
     */
    @AfterAll
    public static void afterAll() {
        // retrieve the JSON file from the Material directory
        List<Material> materials = store.select(jobName, jobTimestamp,
                MetadataPattern.ANY, FileType.JSON);
        // make a html report
        MaterialsBasicReporter reporter = new MaterialsBasicReporter(
                store.getRoot(), jobName);
        Path report = reporter.reportMaterials(materials, "list.html");
        System.out.printf("See %s%n", report.toString());
    }

    /**
     * convert a JSON string in compact format into a JSON string in pretty-print format
     *
     * @param compactJson
     * @return
     */
    static String prettyPrintJsonString(String compactJson) throws JsonSyntaxException {
        JsonElement je = JsonParser.parseString(compactJson);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(je);
    }
}
