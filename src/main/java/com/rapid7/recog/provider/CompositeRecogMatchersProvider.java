package com.rapid7.recog.provider;

import com.rapid7.recog.RecogMatcher;
import com.rapid7.recog.RecogMatchers;
import com.rapid7.recog.RecogType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * An implementation of {@link IRecogMatchersProvider} that composes multiple, sequentially ordered,
 * underlying {@link IRecogMatchersProvider}s. During the {@link #getMatchers(String, RecogType)}
 * call, the first provider in this composite to match will return its {@link RecogMatchers}.
 */
public class CompositeRecogMatchersProvider implements IRecogMatchersProvider {

  /** Providers in insertion order list. */
  private List<IRecogMatchersProvider> providers;

  public CompositeRecogMatchersProvider(IRecogMatchersProvider... providers) {
    this.providers = new ArrayList<>();

    if (providers != null)
      for (IRecogMatchersProvider provider : providers)
        addProvider(provider);
  }

  /**
   * Adds a new provider to this composite, at the end of the current list of providers. This
   * provider has lower priority than any previously added providers.
   *
   * @param provider The provider to add. Must not be {@code null}.
   * @return a reference to this {@link CompositeRecogMatchersProvider}
   */
  public CompositeRecogMatchersProvider addProvider(IRecogMatchersProvider provider) {
    providers.add(requireNonNull(provider));
    return this;
  }

  @Override
  public Collection<RecogMatchers> getMatchers(RecogType type) {
    // TODO: how to implement ... grab all matchers... then map them by name, resolve union, return
    // collection
    return null;
  }

  @Override
  public RecogMatchers getMatchers(String name, RecogType type) {
    // find all matchers that match the name and type specified
    List<RecogMatchers> matchers = providers.stream().map(p -> p.getMatchers(name, type)).filter(Objects::nonNull).collect(toList());

    // if there are no matching matchers
    if (matchers.isEmpty())
      return null;
    // if there is only one match
    if (matchers.size() == 1)
      return matchers.get(0);
    // if there are multiple matches
    else {
      // the matchers need to be consolidated together; iterate over each list of matchers and keep
      // only unique matchers by their pattern; as the providers are ordered based on priority, first one wins;
      // insertion order is maintained through this process
      LinkedHashMap<String, RecogMatcher> uniqueMatchers = new LinkedHashMap<>();
      matchers.forEach(potentialMatchers -> potentialMatchers.forEach(potentialMatcher -> uniqueMatchers.putIfAbsent(potentialMatcher.getPattern(), potentialMatcher)));

      // return a new list of matchers based on the unique, ordered matcher patterns
      RecogMatchers unionedMatchers = new RecogMatchers();
      unionedMatchers.addAll(uniqueMatchers.values());
      return unionedMatchers;
    }
  }
}
