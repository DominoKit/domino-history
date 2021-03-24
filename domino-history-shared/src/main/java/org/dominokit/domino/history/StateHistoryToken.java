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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Implementation for {@link HistoryToken} */
public class StateHistoryToken implements HistoryToken {

  private static final String QUERY_REGEX = "\\?";
  private static final String FRAGMENT_REGEX = "\\#";
  private final String rootPath;
  private List<String> paths = new LinkedList<>();
  private List<Parameter> queryParameters = new LinkedList<>();
  private List<String> fragments = new LinkedList<>();

  /** @param token String, a URL token */
  public StateHistoryToken(String token) {
    this("", token);
  }

  /**
   * @param rootPath String, the root path token
   * @param token String, a URL token
   */
  public StateHistoryToken(String rootPath, String token) {
    if (isNull(token)) throw new TokenCannotBeNullException();
    this.rootPath = isNull(rootPath) ? "" : rootPath.trim();
    String rebasedToken = rebaseToken(rootPath, token);
    this.paths.addAll(asPathsList(rebasedToken));
    this.queryParameters.addAll(asQueryParameters(rebasedToken));
    this.fragments.addAll(parseFragments(rebasedToken));
  }

  private String rebaseToken(String rootPath, String token) {
    if (isNull(rootPath) || rootPath.trim().isEmpty() || !token.startsWith(rootPath)) {
      return token;
    }
    return token.substring(rootPath.length());
  }

  private String getPathToRoot(String token, String root) {
    if (token.isEmpty()) return root;
    return token.endsWith("/") ? token + root : token + "/" + root;
  }

  private List<String> parseFragments(String token) {
    if (token.contains("#") && token.indexOf("#") < token.length() - 1)
      return asPathsList(token.split(FRAGMENT_REGEX)[1]);
    return new LinkedList<>();
  }

  /**
   * @param path The path to check for
   * @return <b>true</b> if the url path part starts with the specified path otherwise returns
   *     <b>false</b>
   */
  @Override
  public boolean startsWithPath(String path) {
    if (isEmpty(path)) return false;
    return startsWith(paths(), asPathsList(path));
  }

  /**
   * @param fragment The fragment to check for
   * @return <b>true</b> if the fragment part of the url starts with the specified fragment
   *     otherwise returns <b>false</b>
   */
  @Override
  public boolean fragmentsStartsWith(String fragment) {
    if (isEmpty(fragment)) return false;
    return startsWith(fragments(), asPathsList(fragment));
  }

  private boolean startsWith(List<String> paths, List<String> targets) {
    if (isValidSize(paths, targets))
      return IntStream.range(0, targets.size()).allMatch(i -> targets.get(i).equals(paths.get(i)));
    return false;
  }

  /**
   * @param path The path to check for
   * @return <b>true</b> if the path part of the url ends with the specified path otherwise returns
   *     <b>false</b>
   */
  @Override
  public boolean endsWithPath(String path) {
    if (isEmpty(path)) return false;
    return endsWith(paths(), asPathsList(path));
  }

  /**
   * @param fragment The fragment to check for
   * @return <b>true</b> if the fragment part of the url ends with the specified fragment otherwise
   *     returns <b>false</b>
   */
  @Override
  public boolean endsWithFragment(String fragment) {
    if (isEmpty(fragment)) return false;
    return endsWith(fragments(), asPathsList(fragment));
  }

  private boolean endsWith(List<String> paths, List<String> targets) {
    if (isValidSize(paths, targets)) return matchEnds(paths, targets);
    return false;
  }

  private boolean matchEnds(List<String> paths, List<String> targets) {
    int offset = paths.size() - targets.size();
    return IntStream.range(0, targets.size())
        .allMatch(i -> targets.get(i).equals(paths.get(i + offset)));
  }

  /**
   * @param path The path to check for
   * @return <b>true</b> if the path part of the url contains the specified path otherwise returns
   *     <b>false</b>
   */
  @Override
  public boolean containsPath(String path) {
    if (isEmpty(path)) return false;
    return contains(paths(), asPathsList(path));
  }

  /**
   * @param fragment The fragment to check for
   * @return <b>true</b> if the fragment part of the url contains the specified fragment otherwise
   *     returns <b>false</b>
   */
  @Override
  public boolean containsFragment(String fragment) {
    if (isEmpty(fragment)) return false;
    return contains(fragments(), asPathsList(fragment));
  }

  private boolean contains(List<String> paths, List<String> targets) {
    return IntStream.rangeClosed(0, paths.size() - targets.size())
        .anyMatch(i -> isOrderedEquals(paths.subList(i, i + targets.size()), targets));
  }

  private boolean isOrderedEquals(List<String> subList, List<String> targets) {
    return IntStream.of(0, subList.size() - 1).allMatch(i -> subList.get(i).equals(targets.get(i)));
  }

  /**
   * @return a list of Strings representing all paths of a url, e.g
   *     <b>http://localhost:8080/a/b/c</b> will return a list contains <b>a</b>, <b>b</b>,
   *     <b>c</b>,
   */
  @Override
  public List<String> paths() {
    return paths;
  }

  /**
   * @return a list of Strings representing all fragments of a url, e.g
   *     <b>http://localhost:8080/a/b/c#d/e/f</b> will return a list contains <b>d</b>, <b>d</b>,
   *     <b>f</b>,
   */
  @Override
  public List<String> fragments() {
    return fragments;
  }

  /**
   * @return the path part of a url, e.g <b>http://localhost:8080/a/b/c</b> will return <b>a/b/c</b>
   */
  @Override
  public String path() {
    return String.join("/", paths());
  }

  /** @return the string representing the whole query part of a token */
  @Override
  public String query() {
    return queryParameters.stream().map(Parameter::asQueryString).collect(Collectors.joining("&"));
  }

  /**
   * @param name name of the query parameter
   * @return <b>True</b> if the token has a query param that has the specified name, otherwise
   *     returns <b>false</b>.
   */
  @Override
  public boolean hasQueryParameter(String name) {
    Optional<Parameter> param =
        queryParameters.stream().filter(parameter -> parameter.key.equals(name)).findFirst();

    if (param.isPresent()) {
      return true;
    } else {
      return false;
    }
  }

  /** @return Key, value map of all query parameters of the token */
  @Override
  public Map<String, List<String>> queryParameters() {
    Map<String, List<String>> parameters = new HashMap<>();
    queryParameters.forEach(parameter -> parameters.put(parameter.key, parameter.value));
    return parameters;
  }

  /**
   * @param name name of the query parameter
   * @return The string value of the query param that has it name as <b>name</b> if found, if not
   *     found it returns null.
   */
  @Override
  public List<String> getQueryParameter(String name) {
    Optional<Parameter> param =
        queryParameters.stream().filter(parameter -> parameter.key.equals(name)).findFirst();

    if (param.isPresent()) {
      return param.get().value;
    } else {
      return null;
    }
  }

  /**
   * Adds a query parameter with specified name and value to the current token, if a query parameter
   * with same name already exists, then replaces its value with the new one
   *
   * @param name query parameter name
   * @param value query parameter value
   * @return {@link HistoryToken} that contains the new query parameter.
   */
  @Override
  public HistoryToken setQueryParameter(String name, String value) {
    if (hasQueryParameter(name)) {
      removeParameter(name);
    }
    appendParameter(name, value);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public HistoryToken setQueryParameter(String name, List<String> values) {
    if (hasQueryParameter(name)) {
      removeParameter(name);
    }
    appendParameter(name, values);
    return this;
  }

  @Override
  public HistoryToken addQueryParameter(String name, String value) {
    return appendParameter(name, value);
  }

  @Override
  public HistoryToken addQueryParameters(String name, List<String> values) {
    return appendParameter(name, values);
  }

  private Parameter getParameter(String name) {
    Optional<Parameter> param =
        queryParameters.stream().filter(parameter -> parameter.key.equals(name)).findFirst();

    if (param.isPresent()) {
      return param.get();
    } else {
      return null;
    }
  }

  /**
   * Appends a new path to the current token instance.
   *
   * @param path the path segment to be appended
   * @return {@link HistoryToken} with appended specified path at the end.
   */
  @Override
  public HistoryToken appendPath(String path) {
    paths.addAll(asPathsList(path));
    return this;
  }

  /**
   * Adds a fragment segment to the end of the fragments part
   *
   * @param fragment to be added
   * @return {@link HistoryToken} with the new fragment segment appended to the end of fragment part
   */
  @Override
  public HistoryToken appendFragment(String fragment) {
    fragments.addAll(asPathsList(fragment));
    return this;
  }

  /**
   * Appends a new query parameter to the end of the token query parameters part.
   *
   * @param name of the query parameter
   * @param value of the query parameter
   * @return {@link HistoryToken} with the new query parameter appended to the end of query part.
   */
  @Override
  public HistoryToken appendParameter(String name, String value) {
    return appendParameter(name, asList(value));
  }

  /** {@inheritDoc} */
  @Override
  public HistoryToken appendParameter(String name, List<String> values) {
    if (nonNull(name) && !name.trim().isEmpty()) {
      if (hasQueryParameter(name)) {
        getParameter(name).addValues(values);
      } else {
        this.queryParameters.add(new Parameter(name, values));
      }
    }
    return this;
  }

  /**
   * Replaces the first occurrence of a path segment with the replacement
   *
   * @param path The path segment to be replaced
   * @param replacement the new path segment
   * @return {@link HistoryToken} with path segment replaced by the replacement
   */
  @Override
  public HistoryToken replacePath(String path, String replacement) {
    List<String> paths = asPathsList(path());
    if (paths.contains(path)) {
      int i = paths.indexOf(path);
      paths.add(i, replacement);
      paths.remove(i + 1);
      this.paths = paths;
    }
    return this;
  }

  /**
   * Replaces the last occurrence of the specified with the replacement
   *
   * @param path the path segment to be replaced
   * @param replacement the new path segment
   * @return {@link HistoryToken} with last occurrence of path segment replaced by the replacement
   */
  @Override
  public HistoryToken replaceLastPath(String path, String replacement) {
    List<String> paths = asPathsList(path());
    if (paths.contains(path)) {
      int i = paths.lastIndexOf(path);
      paths.add(i, replacement);
      paths.remove(i + 1);
      this.paths = paths;
    }
    return this;
  }

  /**
   * Replace a full text part of a path with a the replacement
   *
   * @param path The path part to be replaced
   * @param replacement The new path replacement
   * @return {@link HistoryToken} with the text path replaced with the replacement.
   */
  @Override
  public HistoryToken replacePaths(String path, String replacement) {
    this.paths = asPathsList(path().replace(path, replacement));
    return this;
  }

  /**
   * Replaces the last occurrence of a fragment segment with the specified replacement
   *
   * @param fragment to be replaced
   * @param replacement the new replacement
   * @return {@link HistoryToken} with last occurrence of specified fragment replaced with the
   *     replacement
   */
  @Override
  public HistoryToken replaceFragment(String fragment, String replacement) {

    List<String> fragments = asPathsList(fragment());
    if (fragments.contains(fragment)) {
      int i = fragments.lastIndexOf(fragment);
      fragments.add(i, replacement);
      fragments.remove(i + 1);
      this.fragments = fragments;
    }
    return this;
  }

  /**
   * Replaces the whole specified fragment with a new fragment
   *
   * @param fragment to be replaced
   * @param replacement the new replacement
   * @return {@link HistoryToken} with the text of the fragment specified being replaced with the
   *     replacement.
   */
  @Override
  public HistoryToken replaceFragments(String fragment, String replacement) {
    this.fragments = asPathsList(fragment().replace(fragment, replacement));
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public HistoryToken replaceParameter(
      String name, String replacementName, String replacementValue) {
    return replaceParameter(name, replacementName, asList(replacementValue));
  }

  /** {@inheritDoc} */
  @Override
  public HistoryToken replaceParameter(
      String name, String replacementName, List<String> replacementValue) {
    if (hasQueryParameter(name)) {
      Parameter param = getParameter(name);
      this.queryParameters.add(
          this.queryParameters.indexOf(param), new Parameter(name, replacementValue));
      this.queryParameters.remove(param);
    }
    return this;
  }

  private List<String> asList(String value) {
    List<String> values = new ArrayList<>();
    values.add(value);
    return values;
  }

  /**
   * Replaces the last path segment with a the replacement
   *
   * @param replacement The new replacement to replace the last path segment
   * @return {@link HistoryToken} with last path segment replaced with the replacement
   */
  @Override
  public HistoryToken replaceLastPath(String replacement) {
    if (!this.paths.isEmpty()) {
      this.paths.remove(paths.size() - 1);
      this.paths.add(replacement);
    }
    return this;
  }

  /**
   * Removes the last path segment from the {@link HistoryToken}
   *
   * @return {@link HistoryToken} with last path segment removed.
   */
  @Override
  public HistoryToken removeLastPath() {
    if (!this.paths.isEmpty()) {
      this.paths.remove(paths.size() - 1);
    }
    return this;
  }

  /**
   * Removes the last fragment segment from {@link HistoryToken}
   *
   * @return {@link HistoryToken} with last fragment segment removed.
   */
  @Override
  public HistoryToken removeLastFragment() {
    if (!this.fragments.isEmpty()) {
      this.fragments.remove(fragments.size() - 1);
    }
    return this;
  }

  /**
   * Replaces the last fragment segment with the replacement
   *
   * @param replacement the fragment to be use as a replacement
   * @return {@link HistoryToken} with last fragment replaced with the replacement
   */
  @Override
  public HistoryToken replaceLastFragment(String replacement) {
    if (!this.fragments.isEmpty()) {
      this.fragments.remove(fragments.size() - 1);
      this.fragments.add(replacement);
    }
    return this;
  }

  /**
   * Replace the whole token path with the new path
   *
   * @param newPath the new path segment
   * @return {@link HistoryToken} with its path part being replaced completely with the newPath
   */
  @Override
  public HistoryToken replaceAllPaths(String newPath) {
    this.paths = asPathsList(newPath);
    return this;
  }

  /**
   * Replace the whole token fragment part with the newFragment
   *
   * @param newFragment the new fragment segment
   * @return {@link HistoryToken} with whole fragment part replaced with the newFragment.
   */
  @Override
  public HistoryToken replaceAllFragments(String newFragment) {
    this.fragments = asPathsList(newFragment);
    return this;
  }

  /**
   * Replaces the whole token query part with the newQuery
   *
   * @param newQuery the new query part
   * @return {@link HistoryToken} with whole query part replaced with the newQuery.
   */
  @Override
  public HistoryToken replaceQuery(String newQuery) {
    this.queryParameters = parsedParameters(newQuery);
    return this;
  }

  /**
   * removes the whole query part from the token
   *
   * @return {@link HistoryToken} without a query part
   */
  @Override
  public HistoryToken clearQuery() {
    this.queryParameters.clear();
    return this;
  }

  /**
   * Removes the query parameter with the specified name
   *
   * @param name of the parameter to be removed
   * @return {@link HistoryToken} with the query parameter with the specified name being removed
   */
  @Override
  public HistoryToken removeParameter(String name) {
    Parameter parameter = getParameter(name);
    if (nonNull(parameter)) {
      this.queryParameters.remove(parameter);
    }
    return this;
  }

  /**
   * Removes the whole path part of the {@link HistoryToken}
   *
   * @return {@link HistoryToken} without the path part.
   */
  @Override
  public HistoryToken clearPaths() {
    this.paths.clear();
    return this;
  }

  /**
   * Removes the whole fragment part if the {@link HistoryToken}
   *
   * @return {@link HistoryToken} without the fragments part.
   */
  @Override
  public HistoryToken clearFragments() {
    this.fragments.clear();
    return this;
  }

  /**
   * Removes all path segment that matches the specified path.
   *
   * @param path the path segment to be removed
   * @return {@link HistoryToken} with out all path segments matching the path removed.
   */
  @Override
  public HistoryToken removePath(String path) {
    this.paths.removeAll(asPathsList(path));
    return this;
  }

  /**
   * Removes all occurrences of a fragment segment from the token fragment part
   *
   * @param fragment segment to be removed
   * @return {@link HistoryToken} with all fragment segment that matches the fragment removed.
   */
  @Override
  public HistoryToken removeFragment(String fragment) {
    this.fragments.removeAll(asPathsList(fragment));
    return this;
  }

  /**
   * Clear all of token (path part, query part, fragment part)
   *
   * @return an empty {@link HistoryToken}
   */
  @Override
  public HistoryToken clear() {
    clearPaths();
    clearQuery();
    clearFragments();
    return this;
  }

  /** @return the string representing the whole fragment part of a token */
  @Override
  public String fragment() {
    return String.join("/", fragments());
  }

  /**
   * @return <b>true</b> if all of token (path part, query part, fragments part) are empty,
   *     otherwise return <b>false</b>.
   */
  @Override
  public boolean isEmpty() {
    return paths.isEmpty() && queryParameters.isEmpty() && fragments.isEmpty();
  }

  /** @return the full string representation of a {@link HistoryToken} */
  @Override
  public String value() {
    String path = path();
    String separator =
        (getRootPath().isEmpty()
                || getRootPath().endsWith("/")
                || path.startsWith("/")
                || path.isEmpty())
            ? ""
            : "/";
    return getRootPath() + separator + noRootValue();
  }

  /** {@inheritDoc} */
  @Override
  public String noRootValue() {
    return path() + appendQuery(query()) + appendFragment();
  }

  /**
   * @return <b>true</b> if the token has expression parameters, expression parameters are segments
   *     that starts with <b>:</b> or surrounded with <b>{}</b> .
   */
  @Override
  public boolean hasVariables() {
    String tokenValue = value();
    return tokenValue.contains(":") || tokenValue.contains("{") || tokenValue.contains("]");
  }

  private String appendFragment() {
    return isEmpty(fragment()) ? "" : "#" + fragment();
  }

  private String appendQuery(String query) {
    return isEmpty(query) ? "" : "?" + query;
  }

  private List<String> asPathsList(String token) {
    if (isNull(token) || isEmpty(token) || token.startsWith("?") || token.startsWith("#"))
      return new ArrayList<>();
    return Arrays.stream(splittedPaths(token))
        .filter(p -> !p.isEmpty())
        .collect(Collectors.toCollection(LinkedList::new));
  }

  private String[] splittedPaths(String pathString) {
    return parsePathPart(pathString).split("/");
  }

  private String parsePathPart(String pathString) {
    return pathString.replace("!", "").split(QUERY_REGEX)[0].split(FRAGMENT_REGEX)[0];
  }

  private boolean isEmpty(String path) {
    return isNull(path) || path.isEmpty();
  }

  private boolean isValidSize(List<String> paths, List<String> targets) {
    return !targets.isEmpty() && targets.size() <= paths.size();
  }

  private List<Parameter> asQueryParameters(String token) {

    String queryString = queryPart(token);
    if (isNull(queryString) || queryString.trim().isEmpty()) {
      return new LinkedList<>();
    }
    return parsedParameters(queryString);
  }

  private List<Parameter> parsedParameters(String queryString) {

    return Stream.of(queryString.split("&")).map(part -> part.split("="))
        .collect(
            Collectors.groupingBy(
                keyValue -> keyValue[0],
                LinkedHashMap::new,
                Collectors.mapping(keyValue -> keyValue[1], Collectors.toList())))
        .entrySet().stream()
        .map(entry -> new Parameter(entry.getKey(), entry.getValue()))
        .collect(Collectors.toCollection(LinkedList::new));
  }

  private String queryPart(String token) {
    String query = "";
    if (token.contains("?") && token.indexOf("?") < token.length() - 1) {
      String[] parts = token.split(QUERY_REGEX);

      if (parts.length > 1) {
        if (parts[1].split(FRAGMENT_REGEX).length > 0) {
          query = parts[1].split(FRAGMENT_REGEX)[0];
        } else {
          return query;
        }
      } else {
        query = parts[0].split(FRAGMENT_REGEX)[0];
      }

      if (!query.isEmpty() && !query.contains("=")) {
        throw new InvalidQueryStringException(
            "Query string [" + query + "] is missing '=' operator.");
      }
    }
    return query;
  }

  @Override
  public String getRootPath() {
    return rootPath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof StateHistoryToken)) return false;
    StateHistoryToken that = (StateHistoryToken) o;

    return paths.equals(that.paths)
        && fragments.equals(that.fragments)
        && queryParameters.size() == that.queryParameters.size()
        && queryParameters.containsAll(that.queryParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(paths, queryParameters, fragments);
  }

  private static class Parameter {
    private String key;
    private List<String> value;

    public Parameter(String key, List<String> value) {
      this.key = key;
      this.value = value;
    }

    private void addValues(List<String> moreValues) {
      value.addAll(moreValues);
    }

    private void replaceValues(List<String> newValues) {
      value.clear();
      value.addAll(newValues);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Parameter)) return false;
      Parameter parameter = (Parameter) o;
      return Objects.equals(key, parameter.key)
          && value.size() == parameter.value.size()
          && value.containsAll(parameter.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(key, value);
    }

    private String asQueryString() {
      return value.stream().map(value -> key + "=" + value).collect(Collectors.joining("&"));
    }
  }
}
