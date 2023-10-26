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

/** A POJO representing a query parameter in a {@link HistoryToken} */
public class TokenQueryParameter extends TokenParameter {
  public TokenQueryParameter(String name, String value) {
    super(name, value);
  }

  public static TokenQueryParameter of(String name, String value) {
    return new TokenQueryParameter(name, value);
  }

  public String apply(String token) {
    StateHistoryToken stateHistoryToken = new StateHistoryToken(token);
    stateHistoryToken.appendParameter(getName(), getValue());
    return stateHistoryToken.value();
  }
}
