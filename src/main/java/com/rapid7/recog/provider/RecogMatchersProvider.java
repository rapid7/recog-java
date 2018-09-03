package com.rapid7.recog.provider;

import com.rapid7.recog.RecogMatchers;
import com.rapid7.recog.RecogType;
import com.rapid7.recog.parser.ParseException;
import com.rapid7.recog.parser.RecogParser;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.Objects.requireNonNull;

/**
 * Default provider implementation that retrieves {@link RecogMatchers} from files in a specified
 * directory on the file system.
 */
public class RecogMatchersProvider implements IRecogMatchersProvider, Serializable {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecogMatchersProvider.class);

  private final Map<String, RecogMatchers> matchersByFileName;
  private final Map<String, RecogMatchers> matchersByKey;
  private final RecogType type;
  private final transient RecogParser parser;

  public RecogMatchersProvider(RecogType type, File directory) {
    this(type, directory, new RecogParser());
  }

  @Override
  public Collection<RecogMatchers> getMatchers(RecogType type) {
    if (this.type == type)
      return matchersByKey.values();
    else
      return null;
  }

  @Override
  public RecogMatchers getMatchers(String name, RecogType type) {
    if (this.type == type) {
      // attempt to find the recog matchers by the file name, then key
      RecogMatchers matchers = matchersByFileName.get(name);
      if (matchers == null)
        matchers = matchersByKey.get(name);

      return matchers;
    } else
      return null;
  }

  /**
   * Constructor that allows injection of the parser, for testability.
   */
  RecogMatchersProvider(RecogType type, File directory, RecogParser parser) {
    this.type = requireNonNull(type);
    this.parser = requireNonNull(parser);
    matchersByFileName = new HashMap<>();
    matchersByKey = new HashMap<>();

    parseFiles(requireNonNull(directory));
  }

  /**
   * This method collects fingerprints from a given dir, using a given set of file extension
   * supported. Currently xml.
   *
   * @param dir Location of the file.
   */
  private void parseFiles(File dir) {
    if (dir.isDirectory()) {
      FileUtils.listFiles(dir, new String[] {"xml"}, true).forEach(file -> {
        try {
          RecogMatchers matchers = parser.parse(file);
          matchersByFileName.put(file.getName(), matchers);
          matchersByKey.put(matchers.getKey(), matchers);
        } catch (ParseException exception) {
          LOGGER.warn("Failed to parse document {}.", file, exception);
        }
      });
    } else {
      LOGGER.warn("Directory {} does not exist or is not a directory; fingerprinting may be inaccurate.", dir);
    }

    // Only count matchers loaded by file name since total matcher count will be higher than file count
    LOGGER.info("Loaded {} fingerprint files from {}.", matchersByFileName.size(), dir);
  }
}
