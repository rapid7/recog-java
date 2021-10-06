package com.rapid7.recog;

import com.rapid7.recog.pattern.RecogPatternMatchResult;
import com.rapid7.recog.pattern.RecogPatternMatcher;
import java.util.AbstractMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

public class CustomPatternMatcherTest {

  private static class EchoPatternMatcher implements RecogPatternMatcher {

    @Override
    public String getPattern() {
      return null;
    }

    @Override
    public int getFlags() {
      return 0;
    }

    @Override
    public boolean matches(String input) {
      return true;
    }

    @Override
    public RecogPatternMatchResult match(String input) {
      return new RecogPatternMatchResult() {
        @Override
        public int groupCount() {
          return Integer.MAX_VALUE;
        }

        @Override
        public String group(int index) {
          return "group: " + index;
        }

        @Override
        public String group(String name) {
          return "group: " + name;
        }
      };
    }
  }

  @Test
  public void customMatcherTest() {
    // given
    RecogPatternMatcher patternMatcher = new EchoPatternMatcher();
    RecogMatcher matcher = new RecogMatcher(patternMatcher)
        .addParam(1, "1")
        .addParam(2, "2")
        .addParam("name");

    // when
    Map<String, String> matches = matcher.match("arbitrary text input");

    // then
    assertThat(matches.entrySet(), hasSize(3));
    assertThat(matches.entrySet(), containsInAnyOrder(
        new AbstractMap.SimpleEntry<>("1", "group: 1"),
        new AbstractMap.SimpleEntry<>("2", "group: 2"),
        new AbstractMap.SimpleEntry<>("name", "group: name")
    ));
  }
}
