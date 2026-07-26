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
package org.dominokit.domino.client.history;

import org.dominokit.domino.history.DominoHistory;
import org.dominokit.domino.history.TokenFilter;

/**
 * This class is used by internal API to create listeners for url changes through static factory
 * methods, the class only expose getters for internal memebers.
 */
public class HistoryListener implements DominoHistory.StateListener {

  private final DominoHistory.StateListener listener;
  private final TokenFilter tokenFilter;
  private final boolean removeOnComplete;

  HistoryListener(
      DominoHistory.StateListener listener, TokenFilter tokenFilter, boolean removeOnComplete) {
    this.listener = listener;
    this.tokenFilter = tokenFilter;
    this.removeOnComplete = removeOnComplete;
  }

  @Override
  public void onPopState(DominoHistory.State state) {
    listener.onPopState(state);
  }

  /**
   * The actual listener that will be executed when the url matches the {@link TokenFilter} assigned
   * to this HistoryListener instance.
   *
   * @return {@link DominoHistory.StateListener}
   */
  public DominoHistory.StateListener getListener() {
    return listener;
  }

  /**
   * The token filter to apply the criteria to decide if the listener should be called for the url
   * change.
   *
   * @return {@link TokenFilter}
   */
  public TokenFilter getTokenFilter() {
    return tokenFilter;
  }

  /**
   * A flag to check if the listener should be removed after firing and on completion.
   *
   * @return boolean
   */
  public boolean isRemoveOnComplete() {
    return removeOnComplete;
  }

  public boolean matches(DominoHistory.StateListener other) {
    return this == other || listener == other;
  }

  @Override
  public String toString() {
    return "HistoryListener{listener="
        + listener
        + ", tokenFilter="
        + tokenFilter
        + ", removeOnComplete="
        + removeOnComplete
        + "}";
  }
}
