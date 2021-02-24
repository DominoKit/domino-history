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

import java.util.Map;

/**
 * This is a token wrapper that reads the token and parses and expression parameters and assign then
 * values from a token constant For example : Assume we have a TokenA equals
 * "path1/:path2#fragment1/:fragment2 And we have a TokenB equals
 * "path1/secondPath#fragment1/secondFragment Then when we normalize TokenA and TokenB we assign
 * values to TokenA expression parameters from TokenB path2 = secondPath fragment2 = secondFragment
 */
public interface NormalizedToken {
  /** @return the original token being normalized */
    HistoryToken getToken();

  /** @return the token path parameters including the parsed parameter expresssions. */
    Map<String, String> getPathParameters();

  /**
   * @param name the name of the path parameter
   * @return the value of the path parameter with the specified name.
   */
    String getPathParameter(String name);

  /**
   * @param name the name of the path parameter
   * @return <b>true</b> if path parameters contains a path with the specified name, otherwise
   *     return <b>false</b>
   */
    boolean containsPathParameter(String name);

  /** @return <b>true</b> if token has no path parameters */
    boolean isEmptyPathParameters();

  /** @return Key, Value of all parsed fragment parameters. */
    Map<String, String> getFragmentParameters();

  /**
   * @param name the name of the fragment parameter
   * @return The value of a parsed fragment with specified name if exists, otherwise return
   *     <b>null</b>.
   */
    String getFragmentParameter(String name);

  /**
   * @param name the name of the fragment parameter
   * @return <b>true</b> of the parsed fragments contains a fragment expression with the specified
   *     name, otherwise return <b>false</b>.
   */
    boolean containsFragmentParameter(String name);

  /** @return <b>true</b> if no fragment parameters were parsed, otherwise return <b>false</b>. */
    boolean isEmptyFragmentParameters();
}
