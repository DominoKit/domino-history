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
package org.dominokit.domino.test.history;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.*;
import org.dominokit.domino.history.*;

/** A test implementation of {@link AppHistory} */
public class TestDominoHistory implements AppHistory {

  private final List<HistoryListener> listeners = new ArrayList<>();
  private final Deque<HistoryState> forwards = new LinkedList<>();
  private final Deque<HistoryState> backwards = new LinkedList<>();
  private String rootPath;

  private List<HistoryInterceptor> interceptors = new ArrayList<>();

  public TestDominoHistory() {
    this("");
  }

  public TestDominoHistory(String rootPath) {
    setRootPath(rootPath);
  }

  @Override
  public DirectState listen(StateListener listener) {
    return listen(TokenFilter.any(), listener, false);
  }

  @Override
  public DirectState listen(TokenFilter tokenFilter, StateListener listener) {
    return listen(tokenFilter, listener, false);
  }

  @Override
  public DirectState listen(StateListener listener, boolean removeOnComplete) {
    return listen(TokenFilter.any(), listener, removeOnComplete);
  }

  @Override
  public DirectState listen(
      TokenFilter tokenFilter, StateListener listener, boolean removeOnComplete) {
    listeners.add(new HistoryListener(listener, tokenFilter, removeOnComplete));
    return new DominoDirectState(tokenFilter, currentState(), listener);
  }

  @Override
  public void removeListener(StateListener listener) {
    for (Iterator<HistoryListener> iterator = listeners.iterator(); iterator.hasNext(); ) {
      if (iterator.next().matches(listener)) {
        iterator.remove();
      }
    }
  }

  @Override
  public List<StateListener> getListeners() {
    List<StateListener> registeredListeners = new ArrayList<>(listeners);
    return Collections.unmodifiableList(registeredListeners);
  }

  @Override
  public String toString() {
    List<StateListener> registeredListeners = getListeners();
    StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < registeredListeners.size(); i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(registeredListeners.get(i));
    }
    return builder.append("]").toString();
  }

  private State currentState() {
    if (forwards.isEmpty()) return new TestState(nullState());
    return new TestState(forwards.peek());
  }

  private HistoryState nullState() {
    return new HistoryState("", "");
  }

  private void inform(HistoryState state) {
    if (!isSameRoot(state.token)) {
      return;
    }
    List<HistoryListener> completedListeners = new ArrayList<>();
    listeners.stream()
        .filter(
            l -> {
              NormalizedToken normalized = getNormalizedToken(rootPath, state.token, l);
              if (isNull(normalized)) {
                normalized = new DefaultNormalizedToken(rootPath, state.token);
              }
              return l.getTokenFilter()
                  .filter(
                      new TestState(new HistoryState(normalized.getToken().value(), "test"))
                          .token());
            })
        .forEach(
            listener -> {
              if (listener.isRemoveOnComplete()) {
                completedListeners.add(listener);
              }

              NormalizedToken normalized = getNormalizedToken(rootPath, state.token, listener);
              listener
                  .getListener()
                  .onPopState(
                      new TestState(
                          normalized, new HistoryState(normalized.getToken().value(), "test")));
            });

    listeners.removeAll(completedListeners);
  }

  private boolean isSameRoot(String token) {
    if (this.rootPath.isEmpty()) {
      return true;
    }
    return token.startsWith(rootPath);
  }

  private NormalizedToken getNormalizedToken(
      String rootPath, String token, HistoryListener listener) {
    return listener.getTokenFilter().normalizeToken(rootPath, token);
  }

  @Override
  public void back() {
    if (!backwards.isEmpty()) {
      final HistoryState state = backwards.pop();
      forwards.push(state);
      inform(state);
    }
  }

  @Override
  public void forward() {
    if (!forwards.isEmpty()) {
      final HistoryState state = forwards.pop();
      backwards.push(state);
      inform(state);
    }
  }

  @Override
  public int getHistoryEntriesCount() {
    return backwards.size();
  }

  @Override
  public void pushState(StateToken stateToken) {
    push(stateToken, new TokenParameter[0]);
  }

  @Override
  public void pushState(StateToken stateToken, TokenParameter... parameters) {
    push(stateToken, parameters);
  }

  private void pushState(
      StateToken stateToken, Runnable onPushHandler, TokenParameter... parameters) {
    push(stateToken, onPushHandler, parameters);
  }

  @Override
  public void fireState(StateToken stateToken) {
    fireState(stateToken, new TokenParameter[0]);
  }

  @Override
  public void fireState(StateToken stateToken, TokenParameter... parameters) {
    pushState(stateToken, this::fireCurrentStateHistory, parameters);
  }

  @Override
  public void replaceState(StateToken stateToken) {
    forwards.pop();
    push(stateToken);
  }

  @Override
  public HistoryToken currentToken() {
    if (isNull(forwards.peek())) return new StateHistoryToken(rootPath, "");
    return new StateHistoryToken(rootPath, forwards.peek().token);
  }

  /** {@inheritDoc} */
  @Override
  public String getRootPath() {
    return this.rootPath;
  }

  @Override
  public void setRootPath(String path) {
    this.rootPath = isNull(path) ? "" : path.trim();
  }

  @Override
  public void fireCurrentStateHistory() {
    if (!forwards.isEmpty()) inform(forwards.peek());
  }

  @Override
  public void fireCurrentStateHistory(String title) {
    fireCurrentStateHistory();
  }

  public void initialState(String token, String data) {
    push(StateToken.of(token).data(data));
  }

  private void push(StateToken stateToken, TokenParameter... parameters) {
    push(stateToken, () -> {}, parameters);
  }

  private void push(StateToken stateToken, Runnable onPushHandler, TokenParameter... parameters) {
    InterceptorChain interceptorChain =
        new InterceptorChain(
            interceptors,
            () -> {
              forwards.push(
                  new HistoryState(
                      replaceParameters(stateToken, Arrays.asList(parameters)),
                      stateToken.getData()));
              onPushHandler.run();
            });
    interceptorChain.intercept(new TokenEvent(stateToken));
  }

  private String replaceParameters(StateToken stateToken, List<TokenParameter> parametersList) {
    String result = stateToken.getToken();
    for (TokenParameter parameter : parametersList) {
      result = result.replace(":" + parameter.getName(), parameter.getValue());
    }
    return result;
  }

  private class HistoryListener implements StateListener {
    private final StateListener listener;
    private final TokenFilter tokenFilter;
    private final boolean removeOnComplete;

    private HistoryListener(
        StateListener listener, TokenFilter tokenFilter, boolean removeOnComplete) {
      this.listener = listener;
      this.tokenFilter = tokenFilter;
      this.removeOnComplete = removeOnComplete;
    }

    @Override
    public void onPopState(State state) {
      listener.onPopState(state);
    }

    public StateListener getListener() {
      return listener;
    }

    public TokenFilter getTokenFilter() {
      return tokenFilter;
    }

    public boolean isRemoveOnComplete() {
      return removeOnComplete;
    }

    public boolean matches(StateListener other) {
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

  public Deque<HistoryState> getForwards() {
    return forwards;
  }

  public Deque<HistoryState> getBackwards() {
    return backwards;
  }

  @Override
  public void addInterceptor(HistoryInterceptor interceptor) {
    if (nonNull(interceptor)) {
      this.interceptors.add(interceptor);
    }
  }

  @Override
  public void removeInterceptor(HistoryInterceptor interceptor) {
    if (nonNull(interceptor)) {
      this.interceptors.remove(interceptor);
    }
  }

  @Override
  public void invoke() {
    final HistoryState state = forwards.peek();
    if (nonNull(state)) {
      inform(state);
    }
  }

  @Override
  public void reload() {
    fireCurrentStateHistory();
  }

  private class TestState implements State {

    private final HistoryState historyState;
    private NormalizedToken normalizedToken;

    private TestState(HistoryState historyState) {
      this.historyState = historyState;
    }

    private TestState(NormalizedToken normalizedToken, HistoryState historyState) {
      this.normalizedToken = normalizedToken;
      this.historyState = historyState;
    }

    @Override
    public String rootPath() {
      return TestDominoHistory.this.rootPath;
    }

    @Override
    public HistoryToken token() {
      return new StateHistoryToken(historyState.token);
    }

    @Override
    public Optional<String> data() {
      return Optional.ofNullable(historyState.data);
    }

    @Override
    public String title() {
      return "test title";
    }

    @Override
    public NormalizedToken normalizedToken() {
      return normalizedToken;
    }

    @Override
    public void setNormalizedToken(NormalizedToken normalizedToken) {
      this.normalizedToken = normalizedToken;
    }
  }

  public class HistoryState {
    private final String token;
    private final String data;

    public HistoryState(String token, String data) {
      this.token = token;
      this.data = data;
    }

    public String getToken() {
      return token;
    }

    public String getData() {
      return data;
    }
  }
}
