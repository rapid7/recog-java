package com.rapid7.recog.verify;

import com.rapid7.recog.RecogMatcher;

public class VerifyReporter {

  private final VerifierOptions options;
  private final Formatter formatter;
  private int successCount;
  private int warningCount;
  private int failureCount;

  public VerifyReporter(VerifierOptions options, Formatter formatter) {
    this.options = options;
    this.formatter = formatter;
    resetCounts();
  }

  public Formatter getFormatter() {
    return formatter;
  }

  public int getSuccessCount() {
    return successCount;
  }

  public int getWarningCount() {
    return warningCount;
  }

  public int getFailureCount() {
    return failureCount;
  }

  public void report(int fingerprintCount) {
    if (!options.isQuiet()) {
      summarize(fingerprintCount);
    }
  }

  public void success(String text) {
    successCount++;
    if (options.isDetail()) {
      formatter.successMessage(String.format("%s%s", padding(), text));
    }
  }

  public void warning(String text) {
    if (!options.isWarnings()) {
      return;
    }

    warningCount++;
    formatter.warningMessage(String.format("%s%s", padding(), text));
  }

  public void failure(String text) {
    failureCount++;
    formatter.failureMessage(String.format("%s%s", padding(), text));
  }

  public void printName(RecogMatcher fingerprint) {
    if (options.isDetail() && !fingerprint.getExamples().isEmpty()) {
      String name = fingerprint.getDescription().isEmpty() ? "[unnamed]" : fingerprint.getDescription();
      formatter.statusMessage(String.format("\n%s", name));
    }
  }

  public void summarize(int fingerprintCount) {
    if (options.isDetail()) {
      printFingerprintCount(fingerprintCount);
    }
    printSummary();
  }

  public void printFingerprintCount(int count) {
    formatter.statusMessage(String.format("\nVerified %d fingerprints:", count));
  }

  public void printSummary() {
    colorizeSummary(summaryLine());
  }

  private void resetCounts() {
    successCount = 0;
    failureCount = 0;
    warningCount = 0;
  }

  private String padding() {
    if (options.isDetail()) {
      return "   ";
    }
    return "";
  }

  private String summaryLine() {
    return String.format("SUMMARY: Test completed with %d successful, %d warnings"
            + ", and %d failures", successCount, warningCount, failureCount);
  }

  private void colorizeSummary(String summary) {
    if (failureCount > 0) {
      formatter.failureMessage(summary);
    } else if (warningCount > 0) {
      formatter.warningMessage(summary);
    } else {
      formatter.successMessage(summary);
    }
  }

}