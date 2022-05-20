package com.rapid7.recog.verify;

import com.rapid7.recog.RecogMatchers;
import com.rapid7.recog.parser.ParseException;
import com.rapid7.recog.parser.RecogParser;
import java.io.StringReader;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.jupiter.api.Test;
import static com.rapid7.recog.TestGenerators.anyUTF8String;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecogVerifierTest {
  @Test
  public void verifyNoExampleNoParamsWarnCount() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
        + "<fingerprints matches=\"recog-verifier-test\">\n"
        + "    <fingerprint pattern=\"^(\\w+) Server ([0-9.]+) - ([0-9]+)$\">\n"
        + "        <description>Service Server - no examples or params</description>\n"
        + "    </fingerprint>\n"
        + "</fingerprints>";

    // when
    RecogParser recogParser = new RecogParser(true);
    RecogMatchers matchers = recogParser.parse(new StringReader(xml), anyUTF8String());
    VerifierOptions verifierOpts = new VerifierOptions();
    RecogVerifier verifier = RecogVerifier.create(verifierOpts, matchers, NullOutputStream.NULL_OUTPUT_STREAM);
    verifier.verify();

    // then
    assertEquals(0, verifier.getReporter().getSuccessCount());
    assertEquals(0, verifier.getReporter().getFailureCount());
    assertEquals(1, verifier.getReporter().getWarningCount());
  }

  @Test
  public void verifyNoExampleZeroPositionParamsWarnCount() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
        + "<fingerprints matches=\"recog-verifier-test\">\n"
        + "    <fingerprint pattern=\"^(\\w+) Server ([0-9.]+) - ([0-9]+)$\">\n"
        + "        <description>Service Server - no examples or params</description>\n"
        + "        <param pos=\"0\" name=\"service.vendor\" value=\"VendorName\"/>\n"
        + "        <param pos=\"0\" name=\"service.product\" value=\"ProductName\"/>\n"
        + "    </fingerprint>\n"
        + "</fingerprints>";

    // when
    RecogParser recogParser = new RecogParser(true);
    RecogMatchers matchers = recogParser.parse(new StringReader(xml), anyUTF8String());
    VerifierOptions verifierOpts = new VerifierOptions();
    RecogVerifier verifier = RecogVerifier.create(verifierOpts, matchers, NullOutputStream.NULL_OUTPUT_STREAM);
    verifier.verify();

    // then
    assertEquals(0, verifier.getReporter().getSuccessCount());
    assertEquals(0, verifier.getReporter().getFailureCount());
    assertEquals(1, verifier.getReporter().getWarningCount());
  }

  @Test
  public void verifyNoExampleNonZeroPositionParamsWarnCount() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
        + "<fingerprints matches=\"recog-verifier-test\">\n"
        + "    <fingerprint pattern=\"^(\\w+) Server ([0-9.]+) - ([0-9]+)$\">\n"
        + "        <description>Service Server - no examples or params</description>\n"
        + "        <param pos=\"0\" name=\"service.vendor\" value=\"VendorName\"/>\n"
        + "        <param pos=\"0\" name=\"service.product\" value=\"ProductName\"/>\n"
        + "        <param pos=\"1\" name=\"service.name\"/>\n"
        + "        <param pos=\"2\" name=\"service.version\"/>"
        + "        <param pos=\"3\" name=\"service.version-date\"/>"
        + "    </fingerprint>\n"
        + "</fingerprints>";

    // when
    RecogParser recogParser = new RecogParser(true);
    RecogMatchers matchers = recogParser.parse(new StringReader(xml), anyUTF8String());
    VerifierOptions verifierOpts = new VerifierOptions();
    RecogVerifier verifier = RecogVerifier.create(verifierOpts, matchers, NullOutputStream.NULL_OUTPUT_STREAM);
    verifier.verify();

    // then
    assertEquals(0, verifier.getReporter().getSuccessCount());
    assertEquals(0, verifier.getReporter().getFailureCount());
    assertEquals(1, verifier.getReporter().getWarningCount());
  }

  @Test
  public void verifySuccessfulExample() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
        + "<fingerprints matches=\"recog-verifier-test\">\n"
        + "    <fingerprint pattern=\"^(\\w+) Server ([0-9.]+) - ([0-9]+)$\">\n"
        + "        <description>Service Server - no examples or params</description>\n"
        + "        <example service.name=\"Media\" service.version=\"7.9.3\" service.version-date=\"1631723269\">Media Server 7.9.3 - 1631723269</example>\n"
        + "        <param pos=\"0\" name=\"service.vendor\" value=\"VendorName\"/>\n"
        + "        <param pos=\"0\" name=\"service.product\" value=\"ProductName\"/>\n"
        + "        <param pos=\"1\" name=\"service.name\"/>\n"
        + "        <param pos=\"2\" name=\"service.version\"/>"
        + "        <param pos=\"3\" name=\"service.version-date\"/>"
        + "    </fingerprint>\n"
        + "</fingerprints>";

    // when
    RecogParser recogParser = new RecogParser(true);
    RecogMatchers matchers = recogParser.parse(new StringReader(xml), anyUTF8String());
    VerifierOptions verifierOpts = new VerifierOptions();
    RecogVerifier verifier = RecogVerifier.create(verifierOpts, matchers, NullOutputStream.NULL_OUTPUT_STREAM);
    verifier.verify();

    // then
    assertEquals(1, verifier.getReporter().getSuccessCount());
    assertEquals(0, verifier.getReporter().getFailureCount());
    assertEquals(0, verifier.getReporter().getWarningCount());
  }

  @Test
  public void verify1FailureAnd1SuccessfulExamples() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
        + "<fingerprints matches=\"recog-verifier-test\">\n"
        + "    <fingerprint pattern=\"^(\\w+) Server ([0-9.]+) - ([0-9]+)$\">\n"
        + "        <description>Service Server - no examples or params</description>\n"
        + "        <example>Media Server 1.2.3.4</example>\n"
        + "        <example service.name=\"Media\" service.version=\"7.9.3\" service.version-date=\"1631723269\">Media Server 7.9.3 - 1631723269</example>\n"
        + "        <param pos=\"0\" name=\"service.vendor\" value=\"VendorName\"/>\n"
        + "        <param pos=\"0\" name=\"service.product\" value=\"ProductName\"/>\n"
        + "        <param pos=\"1\" name=\"service.name\"/>\n"
        + "        <param pos=\"2\" name=\"service.version\"/>"
        + "        <param pos=\"3\" name=\"service.version-date\"/>"
        + "    </fingerprint>\n"
        + "</fingerprints>";

    // when
    RecogParser recogParser = new RecogParser(true);
    RecogMatchers matchers = recogParser.parse(new StringReader(xml), anyUTF8String());
    VerifierOptions verifierOpts = new VerifierOptions();
    RecogVerifier verifier = RecogVerifier.create(verifierOpts, matchers, NullOutputStream.NULL_OUTPUT_STREAM);
    verifier.verify();

    // then
    assertEquals(1, verifier.getReporter().getSuccessCount());
    assertEquals(1, verifier.getReporter().getFailureCount());
    assertEquals(0, verifier.getReporter().getWarningCount());
  }

  @Test
  public void verifySuccessfulExampleUntestedParamsFailCount() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
        + "<fingerprints matches=\"recog-verifier-test\">\n"
        + "    <fingerprint pattern=\"^(\\w+) Server ([0-9.]+) - ([0-9]+)$\">\n"
        + "        <description>Service Server - no examples or params</description>\n"
        + "        <example>Media Server 7.9.3 - 1631723269</example>\n"
        + "        <param pos=\"0\" name=\"service.vendor\" value=\"VendorName\"/>\n"
        + "        <param pos=\"0\" name=\"service.product\" value=\"ProductName\"/>\n"
        + "        <param pos=\"1\" name=\"service.name\"/>\n"
        + "        <param pos=\"2\" name=\"service.version\"/>"
        + "        <param pos=\"3\" name=\"service.version-date\"/>"
        + "    </fingerprint>\n"
        + "</fingerprints>";

    // when
    RecogParser recogParser = new RecogParser(true);
    RecogMatchers matchers = recogParser.parse(new StringReader(xml), anyUTF8String());
    VerifierOptions verifierOpts = new VerifierOptions();
    RecogVerifier verifier = RecogVerifier.create(verifierOpts, matchers, NullOutputStream.NULL_OUTPUT_STREAM);
    verifier.verify();

    // then
    assertEquals(1, verifier.getReporter().getSuccessCount());
    assertEquals(3, verifier.getReporter().getFailureCount());
    assertEquals(0, verifier.getReporter().getWarningCount());
  }

  @Test
  public void verify2FailureExamples() throws ParseException {
    // given
    String xml = "<?xml version=\"1.0\"?>\n"
        + "<fingerprints matches=\"recog-verifier-test\">\n"
        + "    <fingerprint pattern=\"^(\\w+) Server ([0-9.]+) - ([0-9]+)$\">\n"
        + "        <description>Service Server - no examples or params</description>\n"
        + "        <example>Media Server 1.2.3.4</example>\n"
        + "        <example service.name=\"Media\" service.version=\"7.9.3\" service.version-date=\"bad-example-value\">Media Server 7.9.3 - 1631723269</example>\n"
        + "        <param pos=\"0\" name=\"service.vendor\" value=\"VendorName\"/>\n"
        + "        <param pos=\"0\" name=\"service.product\" value=\"ProductName\"/>\n"
        + "        <param pos=\"1\" name=\"service.name\"/>\n"
        + "        <param pos=\"2\" name=\"service.version\"/>"
        + "        <param pos=\"3\" name=\"service.version-date\"/>"
        + "    </fingerprint>\n"
        + "</fingerprints>";

    // when
    RecogParser recogParser = new RecogParser(true);
    RecogMatchers matchers = recogParser.parse(new StringReader(xml), anyUTF8String());
    VerifierOptions verifierOpts = new VerifierOptions();
    RecogVerifier verifier = RecogVerifier.create(verifierOpts, matchers, NullOutputStream.NULL_OUTPUT_STREAM);
    verifier.verify();

    // then
    assertEquals(0, verifier.getReporter().getSuccessCount());
    assertEquals(2, verifier.getReporter().getFailureCount());
    assertEquals(0, verifier.getReporter().getWarningCount());
  }
}
