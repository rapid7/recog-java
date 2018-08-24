package com.rapid7.recog.provider;

import com.rapid7.recog.RecogMatchers;
import com.rapid7.recog.RecogType;
import java.util.Collection;

/**
 * Provides access to {@link RecogMatchers} objects. These objects are classified by names and
 * types.
 */
public interface IRecogMatchersProvider {

  /**
   * Returns a {@link RecogMatchers} associated to the given name and type.
   *
   * @param name The name of the matchers, e.g. a file name like "http_servers.xml" or a key like
   *        "operating_system.name". Must not be {@code null}.
   * @param type The provider type. Must not be {@code null}.
   * @return The {@link RecogMatchers} matching the specified name and type, or {@code null} if none
   *         are found.
   */
  public RecogMatchers getMatchers(String name, RecogType type);

  /**
   * Returns a {@link Collection} of {@link RecogMatchers} that have the specified type. Each
   * matcher is guaranteed to have a distinct key, but are not ordered in any meaningful way.
   *
   * @param type The provider type. Must not be {@code null}.
   * @return {@link Collection} of {@link RecogMatchers} matching the type, or {@code null} if none
   *         are found.
   */
  public Collection<RecogMatchers> getMatchers(RecogType type);
}
