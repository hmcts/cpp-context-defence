package uk.gov.moj.defence.util;

import static java.util.regex.Pattern.compile;
import static uk.gov.moj.defence.domain.common.UrnRegex.URN_PATTERN;

import uk.gov.justice.json.generator.value.string.RegexGenerator;

public class UrnGeneratorUtil {
    private static final RegexGenerator regexGenerator = new RegexGenerator(compile(URN_PATTERN));

    private UrnGeneratorUtil() {
    }

    public static String generateUrn() {
        return regexGenerator.next();
    }
}
