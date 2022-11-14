/*
 * Copyright © 2019 Dominokit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dominokit.domino.history;

import static java.util.Objects.nonNull;

import java.util.Arrays;

/**
 * An interface to implement to Filters criteria to be used to decide if a specific {@link
 * org.dominokit.domino.history.DominoHistory.StateListener} should be called or not.
 */
public interface TokenFilter {

  /**
   * @param token The parsed token
   * @return <b>true</b> if the token matches the criteria otherwise return <b>false</b>
   */
  boolean filter(HistoryToken token);

  /**
   * Normalized the token before applying the filter criteria
   *
   * @param token The token to be normalized
   * @return {@link NormalizedToken}, default is <b>null</b>.
   */
  default NormalizedToken normalizeToken(String rootPath, String token) {
    return null;
  }

  /**
   * A static factory to create an exactMatch {@link TokenFilter}
   *
   * @param matchingToken the token apply the filter against
   * @return {@link TokenFilter}
   */
  static TokenFilter exactMatch(String matchingToken) {
    return new TokenFilter.ExactMatchFilter(matchingToken);
  }

  /**
   * A static factory to create an startsWith {@link TokenFilter}
   *
   * @param prefix the token prefix apply the filter against
   * @return {@link TokenFilter}
   */
  static TokenFilter startsWith(String prefix) {
    return new TokenFilter.StartsWithFilter(prefix);
  }

  /**
   * A static factory to create an endsWith {@link TokenFilter}
   *
   * @param postfix the token postfix apply the filter against
   * @return {@link TokenFilter}
   */
  static TokenFilter endsWith(String postfix) {
    return new TokenFilter.EndsWithFilter(postfix);
  }

  /**
   * A static factory to create a contains {@link TokenFilter}
   *
   * @param part the token part apply the filter against
   * @return {@link TokenFilter}
   */
  static TokenFilter contains(String part) {
    return new TokenFilter.ContainsFilter(part);
  }

  /**
   * A static factory to create an any {@link TokenFilter}
   *
   * @return {@link TokenFilter}
   */
  static TokenFilter any() {
    return new TokenFilter.AnyFilter();
  }

  /**
   * A static factory to create an exactFragmentMatch {@link TokenFilter}
   *
   * @param matchingToken the token to apply the filter against
   * @return {@link TokenFilter}
   */
  static TokenFilter exactFragmentMatch(String matchingToken) {
    return new TokenFilter.ExactFragmentFilter(matchingToken);
  }

  /**
   * A static factory to create a startsWithFragment {@link TokenFilter}
   *
   * @param prefix the token fragment prefix to apply the filter against
   * @return {@link TokenFilter}
   */
  static TokenFilter startsWithFragment(String prefix) {
    return new TokenFilter.StartsWithFragmentFilter(prefix);
  }

  /**
   * A static factory to create an endsWithFragment {@link TokenFilter}
   *
   * @param postfix the token fragment postfix to apply the filter against
   * @return {@link TokenFilter}
   */
  static TokenFilter endsWithFragment(String postfix) {
    return new TokenFilter.EndsWithFragmentFilter(postfix);
  }

  /**
   * A static factory to create a containsFragment {@link TokenFilter}
   *
   * @param part the token fragment part to apply the filter against
   * @return {@link TokenFilter}
   */
  static TokenFilter containsFragment(String part) {
    return new TokenFilter.ContainsFragmentFilter(part);
  }

  /**
   * A static factory to create an anyFragment {@link TokenFilter}
   *
   * @return {@link TokenFilter}
   */
  static TokenFilter anyFragment() {
    return new TokenFilter.AnyFragmentFilter();
  }

  /**
   * A static factory to create a hasPathFilter {@link TokenFilter}
   *
   * @param path the token path prefix to apply the filter against
   * @return {@link TokenFilter}
   */
  static TokenFilter hasPathFilter(String path) {
    return new HasPathFilter(path);
  }

  /**
   * A static factory to create a hasPathsFilter {@link TokenFilter}
   *
   * @param paths the token paths to apply the filter against
   * @return {@link TokenFilter}
   */
  static TokenFilter hasPathsFilter(String... paths) {
    return new HasPathsFilter(paths);
  }

  /**
   * A static factory to create an exactPathFilter {@link TokenFilter}
   *
   * @param path the token path to apply the filter against
   * @return {@link TokenFilter}
   */
  static TokenFilter exactPathFilter(String path) {
    return new ExactPathFilter(path);
  }

  /**
   * A static factory to create a startsWithPathFilter {@link TokenFilter}
   *
   * @param path the token path to apply the filter against
   * @return {@link TokenFilter}
   */
  static TokenFilter startsWithPathFilter(String path) {
    return new StartsWithPathFilter(path);
  }

  /**
   * A static factory to create a endsWithPathFilter {@link TokenFilter}
   *
   * @param path the token path to apply the filter against
   * @return {@link TokenFilter}
   */
  static TokenFilter endsWithPathFilter(String path) {
    return new EndsWithPathFilter(path);
  }

  /**
   * A static factory to create an anyPathFilter {@link TokenFilter}
   *
   * @return {@link TokenFilter}
   */
  static TokenFilter anyPathFilter() {
    return new AnyPathFilter();
  }

  /**
   * A static factory to create an isEmpty {@link TokenFilter}
   *
   * @return {@link TokenFilter}
   */
  static TokenFilter isEmpty() {
    return new EmptyFilter();
  }

  /**
   * A static factory to create a queryParam {@link TokenFilter}
   *
   * @param paramName the name of the query parameter to look for
   * @param value the value of the query parameter to apply the filter against.
   * @return {@link TokenFilter}
   */
  static TokenFilter queryParam(String paramName, String value) {
    return new QueryFilter(paramName, value);
  }

  /**
   * A static factory to create a Not {@link TokenFilter}
   *
   * @param tokenFilter the TokenFilter to be negated
   * @return {@link TokenFilter}
   */
  static TokenFilter not(TokenFilter tokenFilter) {
    return new Not(tokenFilter);
  }

  /**
   * A static factory to create a {@link And} TokenFilter
   *
   * @param tokenFilters an Array of filters to be composed in this And TokenFilter
   * @return {@link TokenFilter}
   */
  static TokenFilter and(TokenFilter... tokenFilters) {
    return new And(tokenFilters);
  }

  /**
   * A static factory to create a {@link Or} TokenFilter
   *
   * @param tokenFilters an Array of filters to be composed in this Or TokenFilter
   * @return {@link TokenFilter}
   */
  static TokenFilter or(TokenFilter... tokenFilters) {
    return new Or(tokenFilters);
  }

  /** A token filter that will always return <b>true</b> */
  class AnyFilter implements TokenFilter {
    @Override
    public boolean filter(HistoryToken token) {
      return true;
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return new DefaultNormalizedToken(rootPath, token);
    }
  }

  /**
   * A token filter that will return <b>true</b> only of the whole history token is an exact match
   * of the specified token.
   */
  class ExactMatchFilter implements TokenFilter {
    private final String matchingToken;

    ExactMatchFilter(String matchingToken) {
      this.matchingToken = matchingToken;
    }

    @Override
    public boolean filter(HistoryToken token) {
      return token.value().equals(matchingToken);
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return TokenNormalizer.normalize(rootPath, token, matchingToken);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token starts with the specified
   * prefix.
   */
  class StartsWithFilter implements TokenFilter {
    private final String prefix;

    StartsWithFilter(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public boolean filter(HistoryToken token) {
      return token.noRootValue().startsWith(prefix);
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return TokenNormalizer.normalize(rootPath, token, prefix);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token ends with the specified
   * postfix.
   */
  class EndsWithFilter implements TokenFilter {
    private final String postfix;

    EndsWithFilter(String postfix) {
      this.postfix = postfix;
    }

    @Override
    public boolean filter(HistoryToken token) {
      return token.value().endsWith(postfix);
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return TokenNormalizer.normalizeTail(rootPath, token, postfix);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token contains the specified
   * part.
   */
  class ContainsFilter implements TokenFilter {
    private final String matchingPart;

    ContainsFilter(String matchingPart) {
      this.matchingPart = matchingPart;
    }

    @Override
    public boolean filter(HistoryToken token) {
      return token.value().contains(matchingPart);
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      if (token.contains(":")) {
        throw new UnsupportedOperationException(
            "Contains filter cannot normalize token, please remove all variable from filter!");
      }
      return new DefaultNormalizedToken(rootPath, token);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token fragments part contains
   * the specified part.
   */
  class ContainsFragmentFilter implements TokenFilter {
    private final String matchingPart;

    ContainsFragmentFilter(String matchingPart) {
      this.matchingPart = matchingPart;
    }

    @Override
    public boolean filter(HistoryToken token) {
      return token.fragment().contains(matchingPart);
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      if (token.contains(":")) {
        throw new UnsupportedOperationException(
            "Contains fragment filter cannot normalize token, please remove all variable from filter!");
      }
      return new DefaultNormalizedToken(rootPath, token);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token fargment part is an exact
   * match of the specified part.
   */
  class ExactFragmentFilter implements TokenFilter {
    private final String matchingPart;

    ExactFragmentFilter(String matchingPart) {
      this.matchingPart = matchingPart;
    }

    @Override
    public boolean filter(HistoryToken token) {
      return token.fragment().equals(matchingPart);
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return TokenNormalizer.normalizeFragments(rootPath, token, matchingPart);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token fragment part starts with
   * the specified prefix.
   */
  class StartsWithFragmentFilter implements TokenFilter {
    private final String prefix;

    StartsWithFragmentFilter(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public boolean filter(HistoryToken token) {
      return token.fragment().startsWith(prefix);
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return TokenNormalizer.normalizeFragments(rootPath, token, prefix);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token fragment part ends with
   * the specified postfix.
   */
  class EndsWithFragmentFilter implements TokenFilter {
    private final String postfix;

    EndsWithFragmentFilter(String postfix) {
      this.postfix = postfix;
    }

    @Override
    public boolean filter(HistoryToken token) {
      return token.fragment().endsWith(postfix);
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return TokenNormalizer.normalizeFragmentsTail(rootPath, token, postfix);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token fragment part is not
   * empty.
   */
  class AnyFragmentFilter implements TokenFilter {

    @Override
    public boolean filter(HistoryToken token) {
      return nonNull(token.fragment()) && !token.fragment().isEmpty();
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return new DefaultNormalizedToken(rootPath, token);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token pathes contains the
   * specified path segment.
   */
  class HasPathFilter implements TokenFilter {
    private final String path;

    HasPathFilter(String path) {
      this.path = path;
    }

    @Override
    public boolean filter(HistoryToken token) {
      return token.paths().contains(path);
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      if (token.contains(":")) {
        throw new UnsupportedOperationException(
            "Has path filter cannot normalize token, please remove all variable from filter!");
      }
      return new DefaultNormalizedToken(rootPath, token);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token paths contains all
   * specified path segments in regards of the order.
   */
  class HasPathsFilter implements TokenFilter {
    private final String[] path;

    HasPathsFilter(String... path) {
      this.path = path;
    }

    @Override
    public boolean filter(HistoryToken token) {
      return token.paths().containsAll(Arrays.asList(path));
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      if (token.contains(":")) {
        throw new UnsupportedOperationException(
            "Has paths filter cannot normalize token, please remove all variable from filter!");
      }
      return new DefaultNormalizedToken(rootPath, token);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token path part is an exact
   * match of the specified path.
   */
  class ExactPathFilter implements TokenFilter {
    private final String path;

    ExactPathFilter(String path) {
      this.path = path;
    }

    @Override
    public boolean filter(HistoryToken token) {
      return token.path().equals(path);
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return TokenNormalizer.normalizePaths(rootPath, token, path);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token path part starts with the
   * specified path.
   */
  class StartsWithPathFilter implements TokenFilter {
    private final String path;

    StartsWithPathFilter(String path) {
      this.path = path;
    }

    @Override
    public boolean filter(HistoryToken token) {
      return token.startsWithPath(path);
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return TokenNormalizer.normalizePaths(rootPath, token, path);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token path part ends with the
   * specified path.
   */
  class EndsWithPathFilter implements TokenFilter {
    private final String path;

    EndsWithPathFilter(String path) {
      this.path = path;
    }

    @Override
    public boolean filter(HistoryToken token) {
      return token.path().endsWith(path);
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return TokenNormalizer.normalizePathTail(rootPath, token, path);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token path part is not empty.
   */
  class AnyPathFilter implements TokenFilter {
    @Override
    public boolean filter(HistoryToken token) {
      return nonNull(token.path()) && !token.paths().isEmpty();
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      if (token.contains(":")) {
        throw new UnsupportedOperationException(
            "Has paths filter cannot normalize token, please remove all variable from filter!");
      }
      return new DefaultNormalizedToken(rootPath, token);
    }
  }

  /** A token filter that will return <b>true</b> only if the history token is empty. */
  class EmptyFilter implements TokenFilter {
    @Override
    public boolean filter(HistoryToken token) {
      return token.isEmpty();
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return new DefaultNormalizedToken(rootPath, token);
    }
  }

  /**
   * A token filter that will return <b>true</b> only if the history token has a query parameter
   * with the specified name and value.
   */
  class QueryFilter implements TokenFilter {

    private String queryParam;
    private String value;

    public QueryFilter(String queryParam, String value) {
      this.queryParam = queryParam;
      this.value = value;
    }

    @Override
    public boolean filter(HistoryToken token) {
      return token.hasQueryParameter(queryParam)
          && token.getQueryParameter(queryParam).equals(value);
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return new DefaultNormalizedToken(rootPath, token);
    }
  }

  /** A token filter that negate the result of another token filter */
  class Not implements TokenFilter {

    private final TokenFilter tokenFilter;

    public static Not of(TokenFilter tokenFilter) {
      return new Not(tokenFilter);
    }

    public Not(TokenFilter tokenFilter) {
      this.tokenFilter = tokenFilter;
    }

    @Override
    public boolean filter(HistoryToken historyToken) {
      return !tokenFilter.filter(historyToken);
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return tokenFilter.normalizeToken(rootPath, token);
    }
  }

  /** A token filter that return <b>true</b> only if all composed TokenFilters return true */
  class And implements TokenFilter {

    private final TokenFilter[] tokenFilters;

    public static And of(TokenFilter... tokenFilter) {
      return new And(tokenFilter);
    }

    public And(TokenFilter... tokenFilters) {
      this.tokenFilters = tokenFilters;
    }

    @Override
    public boolean filter(HistoryToken historyToken) {
      return Arrays.stream(tokenFilters).allMatch(tokenFilter -> tokenFilter.filter(historyToken));
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return new DefaultNormalizedToken(rootPath, token);
    }
  }

  /** A token filter that return <b>true</b> if any of the composed TokenFilters return true */
  class Or implements TokenFilter {

    private final TokenFilter[] tokenFilters;

    public static Or of(TokenFilter... tokenFilter) {
      return new Or(tokenFilter);
    }

    public Or(TokenFilter... tokenFilters) {
      this.tokenFilters = tokenFilters;
    }

    @Override
    public boolean filter(HistoryToken historyToken) {
      return Arrays.stream(tokenFilters).anyMatch(tokenFilter -> tokenFilter.filter(historyToken));
    }

    @Override
    public NormalizedToken normalizeToken(String rootPath, String token) {
      return new DefaultNormalizedToken(rootPath, token);
    }
  }
}
