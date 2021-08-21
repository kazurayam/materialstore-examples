package com.kazurayam.materialstoreexamples;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kazurayam.materialstore.DiffArtifacts;
import com.kazurayam.materialstore.FileType;
import com.kazurayam.materialstore.JobName;
import com.kazurayam.materialstore.JobTimestamp;
import com.kazurayam.materialstore.Material;
import com.kazurayam.materialstore.MaterialList;
import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.Metadata;
import com.kazurayam.materialstore.MetadataPattern;
import com.kazurayam.materialstore.Store;
import com.kazurayam.materialstore.Stores;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

class WebAPITestingChronosTest {

    private static Path root = Paths.get("./build/tmp/testOutput/"
            + WebAPITestingChronosTest.class.getSimpleName() + "/store");

    private static Store store;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path dir = root;
        // We do not clean the output directory
        // We will retail the outputs of the previous runs
        // because we are going to perform "Chronological test".
        // We just make sure the root directory exists
        Files.createDirectories(dir);
        //
        store = Stores.newInstance(root);
    }

    @Test
    void test_comparePreviousAndCurrentWebApp()
            throws IOException, InterruptedException, MaterialstoreException,
            URISyntaxException {
        Store store = Stores.newInstance(root);
        JobName jobName = new JobName("test_comparePreviousAndCurrentWebApp");
        JobTimestamp currentTimestamp = JobTimestamp.now();

        // make Web API request to OpenWeatherMap
        // to get a weather forecast data in JSON,
        // save the data into the Materials directory
        doWebInteraction(store, jobName, currentTimestamp,
                ImmutableMap.of("id", "498817"));
        doWebInteraction(store, jobName, currentTimestamp,
                ImmutableMap.of("q", "Hachinohe"));

        // retrieve a previous weather data of the target city
        JobTimestamp baseTimestamp = currentTimestamp.minusSeconds(1);
        JobTimestamp previousTimestamp =
                store.findJobTimestampPriorTo(jobName, baseTimestamp);
        if (previousTimestamp == JobTimestamp.NULL_OBJECT) {
            throw new MaterialstoreException(
                    "JobTimestamp prior to ${baseTimestamp} is not found. We will quit.");
        }
        MaterialList previousData =
                store.select(jobName, previousTimestamp, MetadataPattern.ANY);

        // retrieve the current weather data of the target city
        MaterialList currentData  =
                store.select(jobName, currentTimestamp, MetadataPattern.ANY);

        // make diff
        DiffArtifacts stuffedDiffArtifacts =
                store.makeDiff(previousData, currentData);

        Double criteria = 0.0d;

        int countWarnings = stuffedDiffArtifacts.countWarnings(criteria);
        System.out.printf("countWarnings: %d%n", countWarnings);

        // compile a diff report in HTML
        Path file = store.reportDiffs(jobName, stuffedDiffArtifacts, criteria,
                "index.html");
        System.out.printf("output: %s%n", file.toString());

        // delete files in the Materials directory older than 3 days to save disk space
        int deletedFiles = store.deleteMaterialsOlderThanExclusive(jobName,
                currentTimestamp, 3L, ChronoUnit.DAYS);
        System.out.printf("deleted %d files%n", deletedFiles);
    }

    private static void doWebInteraction(Store store,
                                     JobName jobName,
                                     JobTimestamp jobTimestamp,
                                     Map<String, String> param)
            throws IOException, InterruptedException, URISyntaxException {
        // make query for data to OpenWeatherMap
        OpenWeatherMapClient client = new OpenWeatherMapClient();
        String weatherData = client.getOpenWeatherData(param);

        // save the data into the store
        Metadata metadata = Metadata.builderWithMap(param).build();
        store.write(jobName, jobTimestamp, FileType.JSON,
                metadata, prettyPrintJsonString(weatherData));
    }

    /**
     * convert a JSON string in compact format into a JSON string in pretty-print format
     *
     * @param compactJson
     * @return
     */
    static String prettyPrintJsonString(String compactJson) {
        JsonElement je = JsonParser.parseString(compactJson);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(je);
    }

}
