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

public class StateToken {
  final String token;
  String title;
  String data;

  private StateToken(String token) {
    this.token = token;
  }

  public static StateToken of(String token) {
    return new StateToken(token);
  }

  public static StateToken of(HistoryToken token) {
    return new StateToken(token.value());
  }

  public String getToken() {
    return token;
  }

  public String getTitle() {
    return title;
  }

  public String getData() {
    return data;
  }

  public StateToken title(String title) {
    this.title = title;
    return this;
  }

  public StateToken data(String data) {
    this.data = data;
    return this;
  }
}
