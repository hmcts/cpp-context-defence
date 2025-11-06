package uk.gov.moj.defence.stub;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

public class StubUtil {

    public static String resourceToString(final String path) {
        try {
            return readFileToString(new File("src/test/resources/" + path));
        } catch (final IOException e) {
            fail("Error consuming file from location " + path);
            throw new UncheckedIOException(e);
        }
    }
}


