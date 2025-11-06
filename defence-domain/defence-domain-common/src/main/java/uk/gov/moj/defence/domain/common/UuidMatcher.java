package uk.gov.moj.defence.domain.common;

import java.util.UUID;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UuidMatcher extends TypeSafeMatcher<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UuidMatcher.class);

    @Override
    protected boolean matchesSafely(String string) {
        try {
            UUID.fromString(string);
            return true;
        } catch (IllegalArgumentException e) {
            LOGGER.error("failed to match uuid");
            return false;
        }
    }

    public static UuidMatcher isValidUuid() {
        return new UuidMatcher();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("String does not match UUID format");
    }
}
