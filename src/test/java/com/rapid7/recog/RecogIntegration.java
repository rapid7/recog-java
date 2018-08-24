package com.rapid7.recog;

import com.rapid7.recog.parser.ParseException;
import com.rapid7.recog.parser.RecogParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests for recog. These tests depend on the downloading and extraction of the recog
 * content into the classpath (this is performed with a Maven plugin during the
 * "pre-integration-test" phase).
 */
public class RecogIntegration {

  @Test
  public void allRecogContentParses() throws FileNotFoundException, ParseException {
    // given
    File fingerprintsDirectory = new File(RecogIntegration.class.getResource("fingerprints").getPath());
    RecogParser parser = new RecogParser(true);

    // when
    Collection<File> files = FileUtils.listFiles(fingerprintsDirectory, new String[] {"xml"}, true);
    for (File file : files) {
      // then - the content parses without exceptions
      RecogMatchers matchers = parser.parse(file);
      for (RecogMatcher matcher : matchers) {
        // when - the matcher has examples
        for (String example : matcher.getExamples())
          // then - the example matches
          assertThat("Matcher in " + file + " with pattern '" + matcher.getPattern() + "' does not match example '" + example + "'.", matcher.matches(example), is(true));
      }
    }
  }
}
