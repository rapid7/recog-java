## Recog Java

[![Travis (.org)](https://img.shields.io/travis/rapid7/recog-java.svg)](https://travis-ci.org/rapid7/recog-java) [![Maven Central](https://img.shields.io/maven-central/v/com.rapid7.recog/recog-java.svg)](https://search.maven.org/artifact/com.rapid7.recog/recog-java) [![Javadocs](https://www.javadoc.io/badge/com.rapid7.recog/recog-java.svg)](https://www.javadoc.io/doc/com.rapid7.recog/recog-java)

Java implementation of [Recog](https://github.com/rapid7/recog) that supports parsing and matching.

#### Recog Content

Recog content (the XML files containing matchers) is maintained at the upstream [recog](https://github.com/rapid7/recog) project. The recog-java tests are configured to download a versioned archive of that repository in order to use the content for testing. The content is otherwise not deployed or handled by the recog-java project, so any consumers of recog-java must provide the content they need.

## Getting Started

Add the dependency to your pom file:

```xml
<dependency>
  <groupId>com.rapid7.recog</groupId>
  <artifactId>recog-java</artifactId>
  <version>0.7.0</version>
</dependency>
```

Implement a simple Recog client:

```java
public class RecogClient implements Recog {
  private RecogMatchersProvider provider;

  public RecogClient(File matchersDirectory) {
    this.provider = new RecogMatchersProvider(BUILTIN, matchersDirectory);
  }

  @Override
  public List<RecogMatchResult> fingerprint(String description) {
    List<RecogMatchResult> matches = new ArrayList<>();
    for (RecogMatchers matchers : provider.getMatchers(BUILTIN)) {
      for (RecogMatch match : matchers.getMatches(description)) {
        RecogMatcher matcher = match.getMatcher();
        matches.add(new RecogMatchResult(matchers.getKey(), matchers.getType(), matchers.getProtocol(), matchers.getPreference(), match.getMatcher().getDescription(), matcher.getPattern(), matcher.getExamples(), match.getParameters()));
      }
    }
    return matches;
  }

  @Override
  public RecogVersion refreshContent() {
    throw new UnsupportedOperationException("Not implemented.");
  }
}

```

Fingerprint some input:

```java
RecogClient recog = new RecogClient(new File("path/to/recog/xml/"));
List<RecogMatchResult> matchResults = recog.fingerprint("Apache HTTPD 6.5");
// draw the rest of the owl...
```

#### Configuring Pattern Matching

By default, recog-java uses Java's standard regular expression package, `java.util.regex`. To use a different implementation, users can implement their own `RecogPatternMatcher` instance:

```java
import com.rapid7.recog.pattern.RecogPatternMatcher;

public class CustomPatternMatcher implements RecogPatternMatcher {
  // custom implementation...
}

RecogPatternMatcher patternMatcher = new CustomPatternMatcher("^Apache HTTPD (?<version>.*)$");
RecogMatcher matcher = new RecogMatcher(patternMatcher);
Map<String, String> results = matcher.match("Apache HTTPD 6.5");
```

## Differences from Ruby implementation

This library is not yet at a 1:1 parity with the original [rapid7/recog](https://github.com/rapid7/recog) Ruby implementation.

Missing features:

- Matching against multi-line input strings
- Matching against base64 encoded strings
- Command line tools like `recog_match` and `recog_verify`

## Development

Fork the repository and create a development branch in your fork. _Working from the master branch in your fork is not recommended._

1. Open your favorite IDE or text editor
2. Make some changes
3. Add some tests if needed
4. Run the tests
5. Push your changes
6. Open a pull request

You can use `mvn clean install` to clean compile, run checkstyle, and run all tests.

#### Code Style

recog-java uses a variation of the Google Java code style, enforced with Checkstyle. Please make sure your changes adhere to this style before submitting a pull request.


## Testing

Run `mvn test` or `mvn clean install`.
