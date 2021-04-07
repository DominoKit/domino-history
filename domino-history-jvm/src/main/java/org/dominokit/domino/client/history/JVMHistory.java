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

import static java.util.Objects.isNull;

import java.util.*;
import org.dominokit.domino.history.*;

/**
 * An implementation of the {@link AppHistory} that treat the url virtually as a queue of strings
 * for use in the jvm/test or desktop applications that are not browser based.
 */
public class JVMHistory implements AppHistory {

  private Set<HistoryListener> listeners = new HashSet<>();
  private Deque<HistoryState> forwards = new LinkedList<>();
  private Deque<HistoryState> backwards = new LinkedList<>();
  private final String rootPath;

  public JVMHistory() {
    this("");
  }

  public JVMHistory(String rootPath) {
    this.rootPath = isNull(rootPath) ? "" : rootPath.trim();
  }

  /**
   * Create a listener that will listen for any change in the virtual url.
   *
   * @param listener {@link StateListener}
   * @return {@link DirectState}
   */
  @Override
  public DirectState listen(StateListener listener) {
    return listen(TokenFilter.any(), listener, false);
  }

  /**
   * Create a listener that will listen for any change that matches the criteria defined by the
   * token filter.
   *
   * @param tokenFilter {@link TokenFilter}
   * @param listener {@link StateListener}
   * @return {@link DirectState}
   */
  @Override
  public DirectState listen(TokenFilter tokenFilter, StateListener listener) {
    return listen(tokenFilter, listener, false);
  }

  /**
   * Create a listener that will listen to all changes to the browser url and will be removed after
   * being fired if {removeOnComplete} is true
   *
   * @param listener {@link StateListener}
   * @param removeOnComplete boolean
   * @return {@link DirectState}
   */
  @Override
  public DirectState listen(StateListener listener, boolean removeOnComplete) {
    return listen(TokenFilter.any(), listener, removeOnComplete);
  }

  /**
   * Create a listener that will listen for any change that matches the criteria defined by the
   * token filter and will be removed after being fired if {removeOnComplete} is true.
   *
   * @param tokenFilter {@link TokenFilter}
   * @param listener {@link StateListener}
   * @param removeOnComplete boolean
   * @return {@link DirectState}
   */
  @Override
  public DirectState listen(
      TokenFilter tokenFilter, StateListener listener, boolean removeOnComplete) {
    listeners.add(new HistoryListener(listener, tokenFilter, removeOnComplete));
    return new DominoDirectState(tokenFilter, currentState(), listener);
  }

  /**
   * Manually removes a listener
   *
   * @param listener {@link StateListener}
   */
  @Override
  public void removeListener(StateListener listener) {
    listeners.remove(listener);
  }

  private State currentState() {
    if (forwards.isEmpty()) return new JVMState(nullState());
    return new JVMState(forwards.peek());
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
            listener -> {
              NormalizedToken normalized = getNormalizedToken(rootPath, state.token, listener);
              if (isNull(normalized)) {
                normalized = new DefaultNormalizedToken(state.token);
              }
              return listener.tokenFilter.filter(
                  new JVMState(new HistoryState(normalized.getToken().value(), "")).token());
            })
        .forEach(
            listener -> {
              if (listener.isRemoveOnComplete()) {
                completedListeners.add(listener);
              }

              NormalizedToken normalized = getNormalizedToken(rootPath, state.token, listener);
              listener.listener.onPopState(
                  new JVMState(normalized, new HistoryState(normalized.getToken().value(), "")));
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
    return listener.tokenFilter.normalizeToken(rootPath, token);
  }

  /** Go back one step simulating a back button */
  @Override
  public void back() {
    if (!backwards.isEmpty()) {
      final HistoryState state = backwards.pop();
      forwards.push(state);
      inform(state);
    }
  }

  /** Go forward one step simulating a forward button */
  @Override
  public void forward() {
    if (!forwards.isEmpty()) {
      final HistoryState state = forwards.pop();
      backwards.push(state);
      inform(state);
    }
  }

  /**
   * The count if the current history entries
   *
   * @return int
   */
  @Override
  public int getHistoryEntriesCount() {
    return backwards.size();
  }

  /**
   * Change the virtual url to the specified token without firing url change listeners, sets the
   * title of the new page and assign the data to the new state.
   *
   * @param token The new virtual url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   */
  @Override
  public void pushState(String token, String title, String data) {
    push(token, data, new TokenParameter[0]);
  }

  /**
   * Change the virtual url to the specified token without firing url change listeners, sets the
   * title of the new page and assign the data to the new state. In case the new token has
   * expression parameters in the form <b>:paramName</b> they will be replaced using the
   * <b>parameters</b>
   *
   * @param token The new virtual url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  @Override
  public void pushState(String token, String title, String data, TokenParameter... parameters) {
    push(token, data, parameters);
  }

  /**
   * Change the virtual url to the specified token without firing url change listeners.
   *
   * @param token The new virtual url.
   */
  @Override
  public void pushState(String token) {
    push(token, "", new TokenParameter[0]);
  }

  /**
   * Change the virtual url to the specified token without firing url change listeners. In case the
   * new token has expression parameters in the form <b>:paramName</b> they will be replaced using
   * the <b>parameters</b>
   *
   * @param token The new virtual url.
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  @Override
  public void pushState(String token, TokenParameter... parameters) {
    push(token, "", parameters);
  }

  /**
   * Change the virtual url to the specified token and fire change listeners, sets the title of the
   * new page and assign the data to the new state.
   *
   * @param token The new virtual url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   */
  @Override
  public void fireState(String token, String title, String data) {
    fireState(token, title, data, new TokenParameter[0]);
  }

  /**
   * Change the virtual url to the specified token and fire url change listeners, sets the title of
   * the new page and assign the data to the new state. In case the new token has expression
   * parameters in the form <b>:paramName</b> they will be replaced using the <b>parameters</b>
   *
   * @param token The new virtual url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  @Override
  public void fireState(String token, String title, String data, TokenParameter... parameters) {
    pushState(token, title, data, parameters);
    fireCurrentStateHistory();
  }

  /**
   * Change the virtual url to the specified token and fire url change listeners.
   *
   * @param token The new virtual url.
   */
  @Override
  public void fireState(String token) {
    fireState(token, new TokenParameter[0]);
  }

  /**
   * Change the virtual url to the specified token and fire url change listeners. In case the new
   * token has expression parameters in the form <b>:paramName</b> they will be replaced using the
   * <b>parameters</b>
   *
   * @param token The new virtual url.
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  @Override
  public void fireState(String token, TokenParameter... parameters) {
    pushState(token, parameters);
    fireCurrentStateHistory();
  }

  /**
   * Replace the current virtual url with the specified token without firing url change listeners,
   * sets the title of the new page and assign the data to the new state.
   *
   * @param token The new virtual url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   */
  @Override
  public void replaceState(String token, String title, String data) {
    forwards.pop();
    push(token, data);
  }

  /**
   * Change the virtual url to the specified token without firing url change listeners, sets the
   * title of the new page and assign the data to the new state.
   *
   * @param token {@link HistoryToken} The new virtual url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   */
  @Override
  public void pushState(HistoryToken token, String title, String data) {
    pushState(token.value(), title, data);
  }

  /**
   * Change the virtual url to the specified token without firing url change listeners, sets the
   * title of the new page and assign the data to the new state. In case the new token has
   * expression parameters in the form <b>:paramName</b> they will be replaced using the
   * <b>parameters</b>
   *
   * @param token {@link HistoryToken} The new virtual url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  @Override
  public void pushState(
      HistoryToken token, String title, String data, TokenParameter... parameters) {
    pushState(token.value(), title, data, parameters);
  }

  /**
   * Change the virtual url to the specified token without firing url change listeners.
   *
   * @param token {@link HistoryToken} The new virtual url.
   */
  @Override
  public void pushState(HistoryToken token) {
    pushState(token.value());
  }

  /**
   * Change the virtual url to the specified token without firing url change listeners. In case the
   * new token has expression parameters in the form <b>:paramName</b> they will be replaced using
   * the <b>parameters</b>
   *
   * @param token {@link HistoryToken} The new virtual url.
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  @Override
  public void pushState(HistoryToken token, TokenParameter... parameters) {
    pushState(token.value(), parameters);
  }

  /**
   * Change the virtual url to the specified token and fire change listeners, sets the title of the
   * new page and assign the data to the new state.
   *
   * @param token {@link HistoryToken} The new virtual url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   */
  @Override
  public void fireState(HistoryToken token, String title, String data) {
    fireState(token.value(), title, data);
  }

  /**
   * Change the virtual url to the specified token and fire url change listeners, sets the title of
   * the new page and assign the data to the new state. In case the new token has expression
   * parameters in the form <b>:paramName</b> they will be replaced using the <b>parameters</b>
   *
   * @param token {@link HistoryToken} The new virtual url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  @Override
  public void fireState(
      HistoryToken token, String title, String data, TokenParameter... parameters) {
    fireState(token.value(), title, data, parameters);
  }

  /**
   * Change the virtual url to the specified token and fire url change listeners.
   *
   * @param token {@link HistoryToken} The new virtual url.
   */
  @Override
  public void fireState(HistoryToken token) {
    fireState(token.value());
  }

  /**
   * Change the virtual url to the specified token and fire url change listeners. In case the new
   * token has expression parameters in the form <b>:paramName</b> they will be replaced using the
   * <b>parameters</b>
   *
   * @param token {@link HistoryToken} The new virtual url.
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  @Override
  public void fireState(HistoryToken token, TokenParameter... parameters) {
    fireState(token.value(), parameters);
  }

  /**
   * Replace the current virtual url with the specified token without firing url change listeners,
   * sets the title of the new page and assign the data to the new state.
   *
   * @param token {@link HistoryToken} The new virtual url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   */
  @Override
  public void replaceState(HistoryToken token, String title, String data) {
    replaceState(token.value(), title, data);
  }

  /**
   * Change the virtual url to the specified token without firing url change listeners, sets the
   * title of the new page.
   *
   * @param token The new virtual url.
   * @param title The new page title
   */
  @Override
  public void pushState(String token, String title) {
    pushState(token, title, "");
  }

  /**
   * Change the virtual url to the specified token and fire the url change listeners, sets the title
   * of the new page.
   *
   * @param token The new virtual url.
   * @param title The new page title
   */
  @Override
  public void fireState(String token, String title) {
    fireState(token, title, "");
  }

  /**
   * Change the virtual url to the specified token without firing url change listeners, sets the
   * title of the new page.
   *
   * @param token {@link HistoryToken} The new virtual url.
   * @param title The new page title
   */
  @Override
  public void pushState(HistoryToken token, String title) {
    pushState(token.value(), title, "");
  }

  /**
   * Change the virtual url to the specified token and fire the url change listeners, sets the title
   * of the new page.
   *
   * @param token {@link HistoryToken} The new virtual url.
   * @param title The new page title
   */
  @Override
  public void fireState(HistoryToken token, String title) {
    fireState(token.value(), title, "");
  }

  /**
   * Parse the current virtual url and return an immutable instance of {@link HistoryToken}
   *
   * @return {@link StateHistoryToken}
   */
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

  /**
   * Reapply the current token and virtual url and force calling all listeners with matching token
   * filters.
   */
  @Override
  public void fireCurrentStateHistory() {
    if (!forwards.isEmpty()) inform(forwards.peek());
  }

  /**
   * Reapply the current token and virtual url and force calling all listeners with matching token
   * filters. and use a new page title.
   *
   * @param title The page title
   */
  @Override
  public void fireCurrentStateHistory(String title) {
    fireCurrentStateHistory();
  }

  public void initialState(String token, String data) {
    push(token, data);
  }

  private void push(String token, String data, TokenParameter... parameters) {

    forwards.push(new HistoryState(replaceParameters(token, Arrays.asList(parameters)), data));
  }

  private String replaceParameters(String token, List<TokenParameter> parametersList) {
    String result = token;
    for (TokenParameter parameter : parametersList) {
      result = result.replace(":" + parameter.getName(), parameter.getValue());
    }
    return result;
  }

  private class HistoryListener {
    private final StateListener listener;
    private final TokenFilter tokenFilter;
    private final boolean removeOnComplete;

    public HistoryListener(StateListener listener, TokenFilter tokenFilter) {
      this.listener = listener;
      this.tokenFilter = tokenFilter;
      this.removeOnComplete = false;
    }

    public HistoryListener(
        StateListener listener, TokenFilter tokenFilter, boolean removeOnComplete) {
      this.listener = listener;
      this.tokenFilter = tokenFilter;
      this.removeOnComplete = removeOnComplete;
    }

    public boolean isRemoveOnComplete() {
      return removeOnComplete;
    }
  }

  public Set<HistoryListener> getListeners() {
    return listeners;
  }

  public Deque<HistoryState> getForwards() {
    return forwards;
  }

  public Deque<HistoryState> getBackwards() {
    return backwards;
  }

  private class JVMState implements State {

    private final HistoryState historyState;
    private NormalizedToken normalizedToken;

    private JVMState(HistoryState historyState) {
      this.historyState = historyState;
    }

    private JVMState(NormalizedToken normalizedToken, HistoryState historyState) {
      this.normalizedToken = normalizedToken;
      this.historyState = historyState;
    }

    @Override
    public String rootPath() {
      return JVMHistory.this.rootPath;
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
      return "";
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
