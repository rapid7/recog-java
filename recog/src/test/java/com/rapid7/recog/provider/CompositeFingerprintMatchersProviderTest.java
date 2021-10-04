package com.rapid7.recog.provider;

import com.rapid7.recog.RecogMatcher;
import com.rapid7.recog.RecogMatchers;
import com.rapid7.recog.RecogType;
import org.junit.jupiter.api.Test;
import static com.rapid7.recog.TestGenerators.anyEnum;
import static com.rapid7.recog.TestGenerators.anyString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class CompositeFingerprintMatchersProviderTest {
  
  @Test
  public void getMatchersNoProviders() {
    // given
    String name = anyString();
    RecogType type = anyEnum(RecogType.class);
    CompositeRecogMatchersProvider provider = new CompositeRecogMatchersProvider();

    // when
    RecogMatchers matchers = provider.getMatchers(name, type);

    // then
    assertThat(matchers, is(nullValue()));
  }

  @Test
  public void getMatchersSingleProviderNoMatch() {
    // given
    String name = anyString();
    RecogType type = anyEnum(RecogType.class);
    IRecogMatchersProvider provider = mock(IRecogMatchersProvider.class);
    given(provider.getMatchers(name, type)).willReturn(null);
    CompositeRecogMatchersProvider composite = new CompositeRecogMatchersProvider(provider);

    // when
    RecogMatchers matchers = composite.getMatchers(name, type);

    // then
    assertThat(matchers, is(nullValue()));
  }

  @Test
  public void getMatchersSingleProviderMatch() {
    // given
    String name = anyString();
    RecogType type = anyEnum(RecogType.class);
    IRecogMatchersProvider provider = mock(IRecogMatchersProvider.class);
    RecogMatchers result = mock(RecogMatchers.class);
    given(provider.getMatchers(name, type)).willReturn(result);
    CompositeRecogMatchersProvider composite = new CompositeRecogMatchersProvider(provider);

    // when
    RecogMatchers matchers = composite.getMatchers(name, type);

    // then
    assertThat(matchers, is(result));
  }

  @Test
  public void getMatchersMultipleProviderNoMatch() {
    // given
    String name = anyString();
    RecogType type = anyEnum(RecogType.class);
    IRecogMatchersProvider provider1 = mock(IRecogMatchersProvider.class);
    IRecogMatchersProvider provider2 = mock(IRecogMatchersProvider.class);
    IRecogMatchersProvider provider3 = mock(IRecogMatchersProvider.class);
    given(provider1.getMatchers(name, type)).willReturn(null);
    given(provider2.getMatchers(name, type)).willReturn(null);
    given(provider3.getMatchers(name, type)).willReturn(null);
    CompositeRecogMatchersProvider composite = new CompositeRecogMatchersProvider(provider1, provider2, provider3);

    // when
    RecogMatchers matchers = composite.getMatchers(name, type);

    // then
    assertThat(matchers, is(nullValue()));
  }

  @Test
  public void getMatchersMultipleProviderSingleMatchFirst() {
    // given
    String name = anyString();
    RecogType type = anyEnum(RecogType.class);
    IRecogMatchersProvider provider1 = mock(IRecogMatchersProvider.class);
    IRecogMatchersProvider provider2 = mock(IRecogMatchersProvider.class);
    IRecogMatchersProvider provider3 = mock(IRecogMatchersProvider.class);
    RecogMatchers result = mock(RecogMatchers.class);
    given(provider1.getMatchers(name, type)).willReturn(result);
    given(provider2.getMatchers(name, type)).willReturn(null);
    given(provider3.getMatchers(name, type)).willReturn(null);
    CompositeRecogMatchersProvider composite = new CompositeRecogMatchersProvider(provider1, provider2, provider3);

    // when
    RecogMatchers matchers = composite.getMatchers(name, type);

    // then
    assertThat(matchers, is(result));
  }

  @Test
  public void getMatchersMultipleProviderSingleMatchLast() {
    // given
    String name = anyString();
    RecogType type = anyEnum(RecogType.class);
    IRecogMatchersProvider provider1 = mock(IRecogMatchersProvider.class);
    IRecogMatchersProvider provider2 = mock(IRecogMatchersProvider.class);
    IRecogMatchersProvider provider3 = mock(IRecogMatchersProvider.class);
    RecogMatchers result = mock(RecogMatchers.class);
    given(provider1.getMatchers(name, type)).willReturn(null);
    given(provider2.getMatchers(name, type)).willReturn(null);
    given(provider3.getMatchers(name, type)).willReturn(result);
    CompositeRecogMatchersProvider composite = new CompositeRecogMatchersProvider(provider1, provider2, provider3);

    // when
    RecogMatchers matchers = composite.getMatchers(name, type);

    // then
    assertThat(matchers, is(result));
  }

  @Test
  public void getMatchersMultipleProviderMultipleMatchesFirstIsReturned() {
    // given
    String name = anyString();
    RecogType type = anyEnum(RecogType.class);
    IRecogMatchersProvider provider1 = mock(IRecogMatchersProvider.class);
    IRecogMatchersProvider provider2 = mock(IRecogMatchersProvider.class);
    IRecogMatchersProvider provider3 = mock(IRecogMatchersProvider.class);
    RecogMatcher matcher1 = mock(RecogMatcher.class);
    RecogMatcher matcher2 = mock(RecogMatcher.class);
    RecogMatcher matcher3 = mock(RecogMatcher.class);
    RecogMatchers result1 = new RecogMatchers();
    result1.add(matcher1);
    RecogMatchers result2 = new RecogMatchers();
    result2.add(matcher2);
    RecogMatchers result3 = new RecogMatchers();
    result3.add(matcher3);
    given(provider1.getMatchers(name, type)).willReturn(result1);
    given(provider2.getMatchers(name, type)).willReturn(result2);
    given(provider3.getMatchers(name, type)).willReturn(result3);
    CompositeRecogMatchersProvider composite = new CompositeRecogMatchersProvider(provider1, provider2, provider3);

    // when
    RecogMatchers matchers = composite.getMatchers(name, type);

    // then
    assertThat(matchers, contains(matcher1));
  }

  @Test
  public void getMatchersMultipleProviderMultipleMatchesFirstIsReturnedWhenNotFirst() {
    // given
    String name = anyString();
    RecogType type = anyEnum(RecogType.class);
    IRecogMatchersProvider provider1 = mock(IRecogMatchersProvider.class);
    IRecogMatchersProvider provider2 = mock(IRecogMatchersProvider.class);
    IRecogMatchersProvider provider3 = mock(IRecogMatchersProvider.class);
    RecogMatcher matcher1 = mock(RecogMatcher.class);
    RecogMatcher matcher2 = mock(RecogMatcher.class);
    RecogMatcher matcher3 = mock(RecogMatcher.class);
    RecogMatchers result1 = new RecogMatchers();
    RecogMatchers result2 = new RecogMatchers();
    result2.add(matcher1);
    result2.add(matcher2);
    RecogMatchers result3 = new RecogMatchers();
    result3.add(matcher3);
    given(provider1.getMatchers(name, type)).willReturn(result1);
    given(provider2.getMatchers(name, type)).willReturn(result2);
    given(provider3.getMatchers(name, type)).willReturn(result3);
    CompositeRecogMatchersProvider composite = new CompositeRecogMatchersProvider(provider1, provider2, provider3);

    // when
    RecogMatchers matchers = composite.getMatchers(name, type);

    // then
    assertThat(matchers, contains(matcher1));
  }
  

}
