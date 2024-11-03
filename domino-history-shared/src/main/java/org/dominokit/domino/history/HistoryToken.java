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

import java.util.List;
import java.util.Map;

/** An interface that defines a parsed url token */
public interface HistoryToken {

  /**
   * @param path The path to check for
   * @return <b>true</b> if the url path part starts with the specified path otherwise returns
   *     <b>false</b>
   */
  boolean startsWithPath(String path);

  /**
   * @param fragment The fragment to check for
   * @return <b>true</b> if the fragment part of the url starts with the specified fragment
   *     otherwise returns <b>false</b>
   */
  boolean fragmentsStartsWith(String fragment);

  /**
   * @param path The path to check for
   * @return <b>true</b> if the path part of the url ends with the specified path otherwise returns
   *     <b>false</b>
   */
  boolean endsWithPath(String path);

  /**
   * @param fragment The fragment to check for
   * @return <b>true</b> if the fragment part of the url ends with the specified fragment otherwise
   *     returns <b>false</b>
   */
  boolean endsWithFragment(String fragment);

  /**
   * @param path The path to check for
   * @return <b>true</b> if the path part of the url contains the specified path otherwise returns
   *     <b>false</b>
   */
  boolean containsPath(String path);

  /**
   * @param fragment The fragment to check for
   * @return <b>true</b> if the fragment part of the url contains the specified fragment otherwise
   *     returns <b>false</b>
   */
  boolean containsFragment(String fragment);

  /**
   * @return a list of Strings representing all paths of a url, e.g
   *     <b>http://localhost:8080/a/b/c</b> will return a list contains <b>a</b>, <b>b</b>,
   *     <b>c</b>,
   */
  List<String> paths();

  /**
   * @return a list of Strings representing all fragments of a url, e.g
   *     <b>http://localhost:8080/a/b/c#d/e/f</b> will return a list contains <b>d</b>, <b>d</b>,
   *     <b>f</b>,
   */
  List<String> fragments();

  /**
   * @return the path part of a url, e.g <b>http://localhost:8080/a/b/c</b> will return <b>a/b/c</b>
   */
  String path();

  /**
   * Appends a new path to the current token instance.
   *
   * @param path the path segment to be appended
   * @return {@link HistoryToken} with appended specified path at the end.
   */
  HistoryToken appendPath(String path);

  /**
   * Replaces the first occurrence of a path segment with the replacement
   *
   * @param path The path segment to be replaced
   * @param replacement the new path segment
   * @return {@link HistoryToken} with path segment replaced by the replacement
   */
  HistoryToken replacePath(String path, String replacement);

  /**
   * Replaces the last occurrence of the specified with the replacement
   *
   * @param path the path segment to be replaced
   * @param replacement the new path segment
   * @return {@link HistoryToken} with last occurrence of path segment replaced by the replacement
   */
  HistoryToken replaceLastPath(String path, String replacement);

  /**
   * Replace a full text part of a path with a the replacement
   *
   * @param path The path part to be replaced
   * @param replacement The new path replacement
   * @return {@link HistoryToken} with the text path replaced with the replacement.
   */
  HistoryToken replacePaths(String path, String replacement);

  /**
   * Replaces the last path segment with a the replacement
   *
   * @param replacement The new replacement to replace the last path segment
   * @return {@link HistoryToken} with last path segment replaced with the replacement
   */
  HistoryToken replaceLastPath(String replacement);

  /**
   * Replaces the last fragment segment with the replacement
   *
   * @param replacement the fragment to be use as a replacement
   * @return {@link HistoryToken} with last fragment replaced with the replacement
   */
  HistoryToken replaceLastFragment(String replacement);

  /**
   * Removes the last fragment segment from {@link HistoryToken}
   *
   * @return {@link HistoryToken} with last fragment segment removed.
   */
  HistoryToken removeLastFragment();

  /**
   * Removes the last path segment from the {@link HistoryToken}
   *
   * @return {@link HistoryToken} with last path segment removed.
   */
  HistoryToken removeLastPath();

  /**
   * Replace the whole token path with the new path
   *
   * @param newPath the new path segment
   * @return {@link HistoryToken} with its path part being replaced completely with the newPath
   */
  HistoryToken replaceAllPaths(String newPath);

  /**
   * Remove all paths that appear after the specified path element
   *
   * @param offsetPath the path to remove its tailing paths, the specified offsetPath won't be
   *     removed.
   * @return {@link HistoryToken} with its path part being ends with the specified offsetPath.
   */
  HistoryToken removePathTail(String offsetPath);

  /**
   * Removes the whole path part of the {@link HistoryToken}
   *
   * @return {@link HistoryToken} without the path part.
   */
  HistoryToken clearPaths();

  /**
   * Removes the whole fragment part if the {@link HistoryToken}
   *
   * @return {@link HistoryToken} without the fragments part.
   */
  HistoryToken clearFragments();

  /**
   * Removes all path segment that matches the specified path.
   *
   * @param path the path segment to be removed
   * @return {@link HistoryToken} with out all path segments matching the path removed.
   */
  HistoryToken removePath(String path);

  /** @return Key, value map of all query parameters of the token */
  Map<String, List<String>> queryParameters();

  /**
   * @param name name of the query parameter
   * @return <b>True</b> if the token has a query param that has the specified name, otherwise
   *     returns <b>false</b>.
   */
  boolean hasQueryParameter(String name);

  /**
   * @param name name of the query parameter
   * @return The string value of the query param that has it name as <b>name</b> if found, if not
   *     found it returns null.
   */
  List<String> getQueryParameter(String name);

  /**
   * Adds a query parameter with specified name and value to the current token, if a query parameter
   * with same name already exists, then replaces its value with the new one
   *
   * @param name query parameter name
   * @param value query parameter value
   * @return {@link HistoryToken} that contains the new query parameter.
   */
  HistoryToken setQueryParameter(String name, String value);

  /**
   * Adds a query parameter with specified name and value to the current token, if a query parameter
   * with same name already exists then adds the values to the parameters values
   *
   * @param name query parameter name
   * @param value query parameter value
   * @return {@link HistoryToken} that contains the new query parameter.
   */
  HistoryToken addQueryParameter(String name, String value);

  /**
   * Adds a query parameter with specified name and values to the current token, if a query
   * parameter with same name already exists, then replaces its values with the new one
   *
   * @param name query parameter name
   * @param values query parameter value
   * @return {@link HistoryToken} that contains the new query parameter.
   */
  HistoryToken setQueryParameter(String name, List<String> values);

  /**
   * Adds a query parameter with specified name and value to the current token, if a query parameter
   * with same name already exists then add the values to the existing values
   *
   * @param name query parameter name
   * @param values query parameter value
   * @return {@link HistoryToken} that contains the new query parameter.
   */
  HistoryToken addQueryParameters(String name, List<String> values);

  /**
   * Adds a fragment segment to the end of the fragments part
   *
   * @param fragment to be added
   * @return {@link HistoryToken} with the new fragment segment appended to the end of fragment part
   */
  HistoryToken appendFragment(String fragment);

  /**
   * Appends a new query parameter to the end of the token query parameters part.
   *
   * @param name of the query parameter
   * @param value of the query parameter
   * @return {@link HistoryToken} with the new query parameter appended to the end of query part.
   */
  HistoryToken appendParameter(String name, String value);

  /**
   * Appends a new query parameter to the end of the token query parameters part.
   *
   * @param name of the query parameter
   * @param values List of string values
   * @return {@link HistoryToken} with the new query parameter appended to the end of query part.
   */
  HistoryToken appendParameter(String name, List<String> values);

  /**
   * Replaces the last occurrence of a fragment segment with the specified replacement
   *
   * @param fragment to be replaced
   * @param replacement the new replacement
   * @return {@link HistoryToken} with last occurrence of specified fragment replaced with the
   *     replacement
   */
  HistoryToken replaceFragment(String fragment, String replacement);

  /**
   * Replaces the whole specified fragment with a new fragment
   *
   * @param fragment to be replaced
   * @param replacement the new replacement
   * @return {@link HistoryToken} with the text of the fragment specified being replaced with the
   *     replacement.
   */
  HistoryToken replaceFragments(String fragment, String replacement);

  /**
   * Removes the query parameter with the specified name, and adds a new parameter
   *
   * @param name The name of the parameter to be removed
   * @param replacementName The name of the new parameter
   * @param replacementValue The value of the new parameter
   * @return {@link HistoryToken} with removed parameter with the specified name and a new parameter
   *     added.
   */
  HistoryToken replaceParameter(String name, String replacementName, String replacementValue);

  /**
   * Removes the query parameter with the specified name, and adds a new parameter
   *
   * @param name The name of the parameter to be removed
   * @param replacementName The name of the new parameter
   * @param replacementValue List of String values of the new parameter
   * @return {@link HistoryToken} with removed parameter with the specified name and a new parameter
   *     added.
   */
  HistoryToken replaceParameter(String name, String replacementName, List<String> replacementValue);

  /**
   * Removes the query parameter with the specified name
   *
   * @param name of the parameter to be removed
   * @return {@link HistoryToken} with the query parameter with the specified name being removed
   */
  HistoryToken removeParameter(String name);

  /**
   * Replace the whole token fragment part with the newFragment
   *
   * @param newFragment the new fragment segment
   * @return {@link HistoryToken} with whole fragment part replaced with the newFragment.
   */
  HistoryToken replaceAllFragments(String newFragment);

  /**
   * Replaces the whole token query part with the newQuery
   *
   * @param newQuery the new query part
   * @return {@link HistoryToken} with whole query part replaced with the newQuery.
   */
  HistoryToken replaceQuery(String newQuery);

  /**
   * removes the whole query part from the token
   *
   * @return {@link HistoryToken} without a query part
   */
  HistoryToken clearQuery();

  /** @return the string representing the whole query part of a token */
  String query();

  /** @return the string representing the whole fragment part of a token */
  String fragment();

  /**
   * Removes all occurrences of a fragment segment from the token fragment part
   *
   * @param fragment segment to be removed
   * @return {@link HistoryToken} with all fragment segment that matches the fragment removed.
   */
  HistoryToken removeFragment(String fragment);

  /**
   * @return <b>true</b> if all of token (path part, query part, fragments part) are empty,
   *     otherwise return <b>false</b>.
   */
  boolean isEmpty();

  /**
   * Clear all of token (path part, query part, fragment part)
   *
   * @return an empty {@link HistoryToken}
   */
  HistoryToken clear();

  /** @return the full string representation of a {@link HistoryToken} */
  String value();

  /** @return the full string representation of a {@link HistoryToken} without its rootPath */
  String noRootValue();

  /**
   * @return <b>true</b> if the token has expression parameters, expression parameters are segments
   *     that starts with <b>:</b> or surrounded with <b>{}</b> .
   */
  boolean hasVariables();

  /** @return String, the rootPath of this token if set otherwise return empty String */
  String getRootPath();

  class TokenCannotBeNullException extends RuntimeException {}

  class InvalidQueryStringException extends RuntimeException {
    public InvalidQueryStringException(String query) {
      super(query);
    }
  }
}
