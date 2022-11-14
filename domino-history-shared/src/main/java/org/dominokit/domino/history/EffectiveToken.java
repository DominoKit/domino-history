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

import java.util.Arrays;
import java.util.List;

public class EffectiveToken {

  private String root;
  private StateToken state;
  private final String processedToken;

  public EffectiveToken(String root, StateToken state, TokenParameter... parameters) {
    this.root = root;
    this.state = state;

    String tokenWithParameters = replaceParameters(state.token, Arrays.asList(parameters));
    processedToken = attachRoot(tokenWithParameters);
  }

  private String attachRoot(String token) {
    if (isNull(root) || root.isEmpty() || token.startsWith(root)) {
      return token;
    }

    String separator = (root.endsWith("/") || token.startsWith("/") || token.isEmpty()) ? "" : "/";
    return root + separator + token;
  }

  private String replaceParameters(String token, List<TokenParameter> parametersList) {
    String result = token;
    for (TokenParameter parameter : parametersList) {
      result = result.replace(":" + parameter.getName(), parameter.getValue());
    }
    return result;
  }

  public String getToken() {
    return processedToken;
  }

  public String getTitle() {
    return state.title;
  }

  public String getData() {
    return state.data;
  }
}
