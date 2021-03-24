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

import java.util.List;
import java.util.Map;

/** A utility class to normalize token over another and produce a {@link NormalizedToken} */
public class TokenNormalizer {

  /**
   * @param original the token with expression parameters
   * @param target the token with constant value
   * @return {@link DefaultNormalizedToken}
   */
  public static DefaultNormalizedToken normalize(String original, String target) {
    return normalize("", original, target);
  }

  /**
   * @param rootPath the token root path.
   * @param original the token with expression parameters
   * @param target the token with constant value
   * @return {@link DefaultNormalizedToken}
   */
  public static DefaultNormalizedToken normalize(String rootPath, String original, String target) {
    if (validateToken(target))
      return new DefaultNormalizedToken(new StateHistoryToken(rootPath, original));

    DefaultNormalizedToken normalizedToken = new DefaultNormalizedToken(rootPath, "");
    StateHistoryToken originalToken = new StateHistoryToken(rootPath, original);
    StateHistoryToken targetToken = new StateHistoryToken(rootPath, target);

    normalizePaths(normalizedToken, originalToken, targetToken);
    normalizeParameters(originalToken, targetToken);
    normalizeFragments(normalizedToken, originalToken, targetToken);

    normalizedToken.setToken(originalToken);

    return normalizedToken;
  }

  private static void normalizePaths(
      DefaultNormalizedToken normalizedToken,
      StateHistoryToken originalToken,
      StateHistoryToken targetToken) {
    List<String> originalPaths = originalToken.paths();
    List<String> targetPaths = targetToken.paths();

    int maxIndex =
        originalPaths.size() < targetPaths.size() ? originalPaths.size() : targetPaths.size();
    for (int i = 0; i < maxIndex; i++) {
      checkAndReplacePath(normalizedToken, originalToken, originalPaths, targetPaths, i, i);
    }
  }

  private static void normalizePathsTail(
      DefaultNormalizedToken normalizedToken,
      StateHistoryToken originalToken,
      StateHistoryToken targetToken) {
    List<String> originalPaths = originalToken.paths();
    List<String> targetPaths = targetToken.paths();

    if (originalPaths.size() > 0 && targetPaths.size() > 0) {
      int originalIndex = originalPaths.size() - 1;
      int targetIndex = targetPaths.size() - 1;
      int resultIndex = originalIndex;

      for (int i = targetIndex; resultIndex >= 0 && i >= 0; i--) {
        resultIndex = originalIndex - (targetIndex - i);
        checkAndReplaceLastPath(
            normalizedToken, originalToken, originalPaths, targetPaths, resultIndex, i);
      }
    }
  }

  private static void checkAndReplacePath(
      DefaultNormalizedToken normalizedToken,
      StateHistoryToken originalToken,
      List<String> originalPaths,
      List<String> targetPaths,
      int resultIndex,
      int i) {
    String path = targetPaths.get(i);
    if (path.startsWith(":")) {
      if (resultIndex > -1 && resultIndex < originalPaths.size()) {
        String originalPath = originalPaths.get(resultIndex);
        originalToken.replacePath(originalPath, path);
        normalizedToken.addPathParameter(path.replace(":", ""), originalPath);
      }
    }
  }

  private static void checkAndReplaceLastPath(
      DefaultNormalizedToken normalizedToken,
      StateHistoryToken originalToken,
      List<String> originalPaths,
      List<String> targetPaths,
      int resultIndex,
      int i) {
    String path = targetPaths.get(i);
    if (path.startsWith(":")) {
      if (resultIndex > -1 && resultIndex < originalPaths.size()) {
        String originalPath = originalPaths.get(resultIndex);
        originalToken.replaceLastPath(originalPath, path);
        normalizedToken.addPathParameter(path.replace(":", ""), originalPath);
      }
    }
  }

  private static void normalizeParameters(
      StateHistoryToken originalToken, StateHistoryToken targetToken) {
    Map<String, List<String>> originalParameters = originalToken.queryParameters();
    Map<String, List<String>> targetParameters = targetToken.queryParameters();

    targetParameters.forEach(
        (key, values) ->
            values.forEach(
                value -> {
                  if (value.startsWith(":") && originalParameters.containsKey(key)) {
                    originalToken.replaceParameter(key, key, values);
                  }
                }));
  }

  private static void normalizeFragments(
      DefaultNormalizedToken normalizedToken,
      StateHistoryToken originalToken,
      StateHistoryToken targetToken) {
    List<String> originalFragments = originalToken.fragments();
    List<String> targetFragments = targetToken.fragments();

    int maxIndex =
        originalFragments.size() < targetFragments.size()
            ? originalFragments.size()
            : targetFragments.size();
    for (int i = 0; i < maxIndex; i++) {
      checkAndReplaceFragment(
          normalizedToken, originalToken, originalFragments, targetFragments, i, i);
    }
  }

  private static void normalizeFragmentsTail(
      DefaultNormalizedToken normalizedToken,
      StateHistoryToken originalToken,
      StateHistoryToken targetToken) {
    List<String> originalFragments = originalToken.fragments();
    List<String> targetFragments = targetToken.fragments();

    if (originalFragments.size() > 0 && targetFragments.size() > 0) {
      int originalIndex = originalFragments.size() - 1;
      int targetIndex = targetFragments.size() - 1;
      int resultIndex = originalIndex;

      for (int i = targetIndex; resultIndex >= 0 && i >= 0; i--) {
        resultIndex = originalIndex - (targetIndex - i);
        checkAndReplaceFragment(
            normalizedToken, originalToken, originalFragments, targetFragments, resultIndex, i);
      }
    }
  }

  private static void checkAndReplaceFragment(
      DefaultNormalizedToken normalizedToken,
      StateHistoryToken originalToken,
      List<String> originalFragments,
      List<String> targetFragments,
      int resultIndex,
      int i) {
    String fragment = targetFragments.get(i);
    if (fragment.startsWith(":")) {
      if (resultIndex > -1 && resultIndex < originalFragments.size()) {
        String originalFragment = originalFragments.get(resultIndex);
        originalToken.replaceFragment(originalFragment, fragment);
        normalizedToken.addFragmentParameter(fragment.replace(":", ""), originalFragment);
      }
    }
  }

  /**
   * Normalize the tail of the specified tokens
   *
   * @param original the token with expression parameters
   * @param target the token with constant value
   * @return {@link NormalizedToken}
   */
  public static NormalizedToken normalizeTail(String original, String target) {
    return normalizeTail("", original, target);
  }
  /**
   * Normalize the tail of the specified tokens
   *
   * @param rootPath the token rootPath
   * @param original the token with expression parameters
   * @param target the token with constant value
   * @return {@link NormalizedToken}
   */
  public static NormalizedToken normalizeTail(String rootPath, String original, String target) {
    if (validateToken(target))
      return new DefaultNormalizedToken(new StateHistoryToken(rootPath, original));

    DefaultNormalizedToken normalizedToken = new DefaultNormalizedToken(rootPath, "");
    StateHistoryToken originalToken = new StateHistoryToken(rootPath, original);
    StateHistoryToken targetToken = new StateHistoryToken(rootPath, target);

    normalizePathsTail(normalizedToken, originalToken, targetToken);
    normalizeParameters(originalToken, targetToken);
    normalizeFragmentsTail(normalizedToken, originalToken, targetToken);

    normalizedToken.setToken(originalToken);

    return normalizedToken;
  }

  private static boolean validateToken(String target) {
    if (isNull(target) || target.trim().isEmpty() || !target.contains(":")) return true;
    return false;
  }

  /**
   * Normalize the tail of the specified tokens fragment parts
   *
   * @param original the token with expression parameters
   * @param target the token with constant value
   * @return {@link NormalizedToken}
   */
  public static NormalizedToken normalizeFragmentsTail(String original, String target) {
    return normalizeFragmentsTail("", original, target);
  }
  /**
   * Normalize the tail of the specified tokens fragment parts
   *
   * @param rootPath the token root path
   * @param original the token with expression parameters
   * @param target the token with constant value
   * @return {@link NormalizedToken}
   */
  public static NormalizedToken normalizeFragmentsTail(
      String rootPath, String original, String target) {
    if (validateToken(target))
      return new DefaultNormalizedToken(new StateHistoryToken(rootPath, original));

    DefaultNormalizedToken normalizedToken = new DefaultNormalizedToken(rootPath, "");
    StateHistoryToken originalToken = new StateHistoryToken(rootPath, original);
    StateHistoryToken targetToken = new StateHistoryToken(rootPath, target);

    normalizeFragmentsTail(normalizedToken, originalToken, targetToken);

    normalizedToken.setToken(originalToken);

    return normalizedToken;
  }

  /**
   * Normalize the tail of the specified tokens path part
   *
   * @param original the token with expression parameters
   * @param target the token with constant value
   * @return {@link NormalizedToken}
   */
  public static NormalizedToken normalizePathTail(String original, String target) {
    return normalizePathTail("", original, target);
  }

  /**
   * Normalize the tail of the specified tokens path part
   *
   * @param rootPath the token root path
   * @param original the token with expression parameters
   * @param target the token with constant value
   * @return {@link NormalizedToken}
   */
  public static NormalizedToken normalizePathTail(String rootPath, String original, String target) {
    if (validateToken(target))
      return new DefaultNormalizedToken(new StateHistoryToken(rootPath, original));

    DefaultNormalizedToken normalizedToken = new DefaultNormalizedToken(rootPath, "");
    StateHistoryToken originalToken = new StateHistoryToken(rootPath, original);
    StateHistoryToken targetToken = new StateHistoryToken(rootPath, target);

    normalizePathsTail(normalizedToken, originalToken, targetToken);

    normalizedToken.setToken(originalToken);

    return normalizedToken;
  }

  /**
   * Normalize the tail of the specified tokens paths
   *
   * @param original the token with expression parameters
   * @param target the token with constant value
   * @return {@link NormalizedToken}
   */
  public static DefaultNormalizedToken normalizePaths(String original, String target) {
    return normalizePaths("", original, target);
  }
  /**
   * Normalize the tail of the specified tokens paths
   *
   * @param rootPath the token root path
   * @param original the token with expression parameters
   * @param target the token with constant value
   * @return {@link NormalizedToken}
   */
  public static DefaultNormalizedToken normalizePaths(
      String rootPath, String original, String target) {
    if (validateToken(target))
      return new DefaultNormalizedToken(new StateHistoryToken(rootPath, original));

    DefaultNormalizedToken normalizedToken = new DefaultNormalizedToken(rootPath, "");
    StateHistoryToken originalToken = new StateHistoryToken(rootPath, original);
    StateHistoryToken targetToken = new StateHistoryToken(rootPath, target);

    normalizePaths(normalizedToken, originalToken, targetToken);

    normalizedToken.setToken(originalToken);

    return normalizedToken;
  }

  /**
   * Normalize the tail of the specified tokens fragments
   *
   * @param original the token with expression parameters
   * @param target the token with constant value
   * @return {@link NormalizedToken}
   */
  public static DefaultNormalizedToken normalizeFragments(String original, String target) {
    return normalizeFragments("", original, target);
  }

  /**
   * Normalize the tail of the specified tokens fragments
   *
   * @param rootPath the token root path
   * @param original the token with expression parameters
   * @param target the token with constant value
   * @return {@link NormalizedToken}
   */
  public static DefaultNormalizedToken normalizeFragments(
      String rootPath, String original, String target) {
    if (validateToken(target))
      return new DefaultNormalizedToken(new StateHistoryToken(rootPath, original));

    DefaultNormalizedToken normalizedToken = new DefaultNormalizedToken(rootPath, "");
    StateHistoryToken originalToken = new StateHistoryToken(rootPath, original);
    StateHistoryToken targetToken = new StateHistoryToken(rootPath, target);

    normalizeFragments(normalizedToken, originalToken, targetToken);

    normalizedToken.setToken(originalToken);

    return normalizedToken;
  }

  /**
   * Normalize the tail of the specified tokens parameters
   *
   * @param original the token with expression parameters
   * @param target the token with constant value
   * @return {@link NormalizedToken}
   */
  public static DefaultNormalizedToken normalizeParameters(String original, String target) {
    return normalizeParameters("", original, target);
  }

  /**
   * Normalize the tail of the specified tokens parameters
   *
   * @param rootPath the token root path
   * @param original the token with expression parameters
   * @param target the token with constant value
   * @return {@link NormalizedToken}
   */
  public static DefaultNormalizedToken normalizeParameters(
      String rootPath, String original, String target) {
    if (validateToken(target))
      return new DefaultNormalizedToken(new StateHistoryToken(rootPath, original));

    DefaultNormalizedToken normalizedToken = new DefaultNormalizedToken(rootPath, "");
    StateHistoryToken originalToken = new StateHistoryToken(rootPath, original);
    StateHistoryToken targetToken = new StateHistoryToken(rootPath, target);

    normalizeParameters(originalToken, targetToken);

    normalizedToken.setToken(originalToken);

    return normalizedToken;
  }
}
