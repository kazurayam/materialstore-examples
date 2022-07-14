package misc;

import com.kazurayam.materialstore.tutorial.TestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HowJUnitWorks extends TestHelper {

    private static Logger logger = LoggerFactory.getLogger(HowJUnitWorks.class);

    public HowJUnitWorks() {
        super();
        logger.info("[HowJUnitWork] constructed");
    }

    @BeforeAll
    public static void beforeAll() {
        logger.info("[beforeAll] called");
    }

    @BeforeEach
    public void beforeEach() {
        logger.info("[beforeEach]");
    }

    @Test
    public void test_foo() {
        logger.info("[test_foo]");
    }

    @Test
    public void test_bar() {
        logger.info("[test_bar]");
    }

    @AfterEach
    public void afterEach() {
        logger.info("[afterEach]");
    }

    @AfterAll
    public static void afterAll() {
        logger.info("[afterAll]");
    }
}
