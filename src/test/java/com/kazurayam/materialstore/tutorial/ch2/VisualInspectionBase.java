package com.kazurayam.materialstore.tutorial.ch2;

import com.kazurayam.materialstore.Inspector;
import com.kazurayam.materialstore.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.JobName;
import com.kazurayam.materialstore.filesystem.JobTimestamp;
import com.kazurayam.materialstore.filesystem.MaterialList;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.materialize.MaterializingPageFunctions;
import com.kazurayam.materialstore.materialize.StorageDirectory;
import com.kazurayam.materialstore.materialize.Target;
import com.kazurayam.materialstore.materialize.TargetCSVReader;
import com.kazurayam.materialstore.reduce.MProductGroup;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

public abstract class VisualInspectionBase {

    private static Logger logger = LoggerFactory.getLogger(VisualInspectionBase.class);

    protected static Store store;
    protected static JobName jobName;

    protected WebDriver driver;

    /**
     *
     */
    protected MaterialList materialize(String csvText, Store store, JobName jobName)
            throws MaterialstoreException {
        List<Target> targetList = TargetCSVReader.parse(csvText);
        JobTimestamp jobTimestamp = JobTimestamp.now();
        StorageDirectory storageDirectory =
                new StorageDirectory(store, jobName, jobTimestamp);
        for (Target target : targetList) {
            MaterializingPageFunctions.storeEntirePageScreenshot
                    .accept(target, driver, storageDirectory);
            MaterializingPageFunctions.storeHTMLSource
                    .accept(target, driver, storageDirectory);
        }
        return store.select(jobName, jobTimestamp);
    }

    /**
     *
     */
    protected int report(Store store, MProductGroup mProductGroup, Double criteria)
            throws MaterialstoreException {
        JobName jobName = mProductGroup.getJobName();
        logger.info(String.format("[report] started; criteria=%.2f, mProductGroup=%s, jobName=%s, store=%s",
                criteria, mProductGroup.getDescription(),
                jobName.toString(), store.toString()));
        logger.info("[report] mProductGroup=" + mProductGroup.toJson(true));
        // the file name of HTML report
        String fileName = jobName.toString() + "-index.html";
        Inspector inspector = Inspector.newInstance(store);
        Path report = inspector.report(mProductGroup, criteria, fileName);
        logger.info("[report] The report can be found at " + report.toString());
        int warnings = mProductGroup.countWarnings(criteria);
        return warnings;
    }

}
