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

public class HistoryListener {
  private final DominoHistory.StateListener listener;

  private final TokenFilter tokenFilter;

  private final boolean removeOnComplete;

  private HistoryListener(DominoHistory.StateListener listener, TokenFilter tokenFilter) {
    this.listener = listener;
    this.tokenFilter = tokenFilter;
    this.removeOnComplete = false;
  }

  HistoryListener(
      DominoHistory.StateListener listener, TokenFilter tokenFilter, boolean removeOnComplete) {
    this.listener = listener;
    this.tokenFilter = tokenFilter;
    this.removeOnComplete = removeOnComplete;
  }

  public DominoHistory.StateListener getListener() {
    return listener;
  }

  public TokenFilter getTokenFilter() {
    return tokenFilter;
  }

  public boolean isRemoveOnComplete() {
    return removeOnComplete;
  }
}
