package com.rapid7.recog.provider;

import com.rapid7.recog.RecogMatchers;
import com.rapid7.recog.RecogType;
import com.rapid7.recog.parser.ParseException;
import com.rapid7.recog.parser.RecogParser;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
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

  public RecogMatchersProvider(RecogType type, Path path) {
    this(type, path, new RecogParser());
  }

  public RecogMatchersProvider(RecogType type, File directory) {
    this(type, directory.toPath());
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
  RecogMatchersProvider(RecogType type, Path path, RecogParser parser) {
    this.type = requireNonNull(type);
    this.parser = requireNonNull(parser);
    matchersByFileName = new HashMap<>();
    matchersByKey = new HashMap<>();

    parseFiles(requireNonNull(path));
  }

  /**
   * This method collects fingerprints from a given directory, file or zip, using a
   * given set of file extension supported. Currently xml.
   *
   * @param path The location of the file, directory or zip to load.
   */
  private void parseFiles(Path path) {
    FileSystem fileSystem = null;
    if (Files.isDirectory(path)) {
      parseFromWalkablePath(path);
    } else if (Files.isRegularFile(path) && path.getFileName().toString().endsWith(".zip")) {
      try (FileSystem fs = FileSystems.newFileSystem(path, (java.lang.ClassLoader)null)) {
        parseFromWalkablePath(fs.getRootDirectories().iterator().next());
      } catch (IOException exception) {
        LOGGER.warn("Failed to open zip file {}.", path, exception);
      }
    } else {
      LOGGER.warn("Path {} does not exist or is not walkable; fingerprinting may be inaccurate.", path);
    }

    // Only count matchers loaded by file name since total matcher count will be higher than file count
    LOGGER.info("Loaded {} fingerprint files from {}.", matchersByFileName.size(), path);
  }

  /**
   * Parse a path that represents some walkable location (zip file, directory, etc.).
   *
   * @param path The walkable path to parse.
   */
  private void parseFromWalkablePath(Path path) {
    final PathMatcher filter = path.getFileSystem().getPathMatcher("glob:**/*.xml");
    try (Stream<Path> files = Files.walk(path)) {
      files.filter(filter::matches).forEach(file -> {
        try {
          final String fileName = file.getFileName().toString();
          try (Reader reader = Files.newBufferedReader(file)) {
            int extIndex = fileName.lastIndexOf(".xml");
            RecogMatchers matchers = parser.parse(reader, extIndex > 0 ? fileName.substring(0, extIndex) : fileName);
            if (matchers != null) {
              matchersByFileName.put(fileName, matchers);
              matchersByKey.put(matchers.getKey(), matchers);
            } else {
              LOGGER.warn("Failed to parse file {}. Not adding to matchers.", file);
            }
          }
        } catch (IOException | ParseException exception) {
          LOGGER.warn("Failed to parse document {}.", file, exception);
        }
      });
    } catch (IOException exception) {
      LOGGER.warn("I/O error while attempting to list {}.", path, exception);
    }
  }
}
