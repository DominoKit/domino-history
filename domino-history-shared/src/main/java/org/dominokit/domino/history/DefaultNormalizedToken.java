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

import java.util.HashMap;
import java.util.Map;

public class DefaultNormalizedToken implements NormalizedToken {

  private HistoryToken token = new StateHistoryToken("");
  private final Map<String, String> pathParameters = new HashMap<>();
  private final Map<String, String> fragmentParameters = new HashMap<>();

  public DefaultNormalizedToken() {}

  public DefaultNormalizedToken(HistoryToken token) {
    this.token = token;
  }

  public DefaultNormalizedToken(String token) {
    this.token = new StateHistoryToken(token);
  }

  @Override
  public HistoryToken getToken() {
    return token;
  }

  public void setToken(HistoryToken token) {
    this.token = token;
  }

  @Override
  public Map<String, String> getPathParameters() {
    return pathParameters;
  }

  public void addPathParameter(String name, String value) {
    pathParameters.put(name, value);
  }

  @Override
  public String getPathParameter(String name) {
    return pathParameters.get(name);
  }

  @Override
  public boolean containsPathParameter(String name) {
    return pathParameters.containsKey(name);
  }

  @Override
  public boolean isEmptyPathParameters() {
    return pathParameters.isEmpty();
  }

  @Override
  public Map<String, String> getFragmentParameters() {
    return fragmentParameters;
  }

  public void addFragmentParameter(String name, String value) {
    fragmentParameters.put(name, value);
  }

  @Override
  public String getFragmentParameter(String name) {
    return fragmentParameters.get(name);
  }

  @Override
  public boolean containsFragmentParameter(String name) {
    return fragmentParameters.containsKey(name);
  }

  @Override
  public boolean isEmptyFragmentParameters() {
    return fragmentParameters.isEmpty();
  }
}
