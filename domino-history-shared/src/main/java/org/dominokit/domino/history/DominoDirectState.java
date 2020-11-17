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
import static org.dominokit.domino.history.DominoHistory.*;

import java.util.function.Consumer;

public class DominoDirectState implements DirectState {

  private final TokenFilter tokenFilter;
  private final State state;
  private StateListener listener;
  private Consumer<DominoDirectState> onCompleted = dominoDirectState -> {};

  public DominoDirectState(TokenFilter tokenFilter, State state, StateListener listener) {
    this.tokenFilter = tokenFilter;
    this.state = state;
    this.listener = listener;
  }

  public DominoDirectState onCompleted(Consumer<DominoDirectState> onCompleted) {
    this.onCompleted = onCompleted;
    return this;
  }

  @Override
  public void onDirectUrl() {
    onDirectUrl(tokenFilter);
  }

  @Override
  public void onDirectUrl(TokenFilter tokenFilter) {
    NormalizedToken normalized = tokenFilter.normalizeToken(state.token().value());
    if (isNull(normalized)) {
      normalized = new DefaultNormalizedToken(state.token());
    }
    state.setNormalizedToken(normalized);
    if (tokenFilter.filter(new StateHistoryToken(normalized.getToken().value()))) {
      listener.onPopState(state);
      onCompleted.accept(this);
    }
  }
}
