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
import static java.util.Objects.nonNull;

import elemental2.core.JsMap;
import elemental2.dom.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsinterop.base.Js;
import org.dominokit.domino.history.*;

/** The browser implementation of the {@link AppHistory} */
public class StateHistory implements AppHistory {

  private static final Logger LOGGER = Logger.getLogger(StateHistory.class.getName());

  private Set<HistoryListener> listeners = new HashSet<>();
  private final History history = Js.cast(DomGlobal.self.history);

  /** Default constructor */
  public StateHistory() {
    DomGlobal.self.addEventListener(
        "popstate",
        event -> {
          if (isInformOnPopState()) {
            PopStateEvent popStateEvent = Js.cast(event);
            JsState state = Js.cast(popStateEvent.state);
            if (nonNull(state) && nonNull(state.historyToken)) {
              inform(state.historyToken, state.title, state.data);
            } else {
              inform(windowToken(), windowTitle(), "");
            }
          }
        });

    DomGlobal.document.addEventListener(
        "domino-history-event",
        evt -> {
          CustomEvent tokenEvent = Js.uncheckedCast(evt);
          JsMap<String, String> tokenMap = Js.uncheckedCast(tokenEvent.detail);
          String token = tokenMap.get("token");
          String title = tokenMap.get("title");
          String stateJson = tokenMap.get("stateJson");
          callListeners(token, title, stateJson);
        });
  }

  private void callListeners(String token, String title, String stateJson) {
    final List<HistoryListener> completedListeners = new ArrayList<>();
    listeners.stream()
        .filter(
            l -> {
              NormalizedToken normalized = getNormalizedToken(token, l);
              if (isNull(normalized)) {
                normalized = new DefaultNormalizedToken(token);
              }
              return l.getTokenFilter()
                  .filter(
                      new DominoHistoryState(normalized.getToken().value(), title, stateJson)
                          .token);
            })
        .forEach(
            l -> {
              if (l.isRemoveOnComplete()) {
                completedListeners.add(l);
              }
              DomGlobal.setTimeout(
                  p0 -> {
                    NormalizedToken normalized = getNormalizedToken(token, l);
                    l.getListener()
                        .onPopState(new DominoHistoryState(normalized, token, title, stateJson));
                  },
                  0);
            });

    listeners.removeAll(completedListeners);
  }

  private void inform(String token, String title, String stateJson) {
    try {
      JsMap<String, String> tokenMap = new JsMap<>();
      tokenMap.set("token", token);
      tokenMap.set("title", title);
      tokenMap.set("stateJson", stateJson);

      CustomEventInit tokenEventInit = CustomEventInit.create();
      tokenEventInit.setDetail(tokenMap);

      CustomEvent tokenEvent = new CustomEvent("domino-history-event", tokenEventInit);
      DomGlobal.document.dispatchEvent(tokenEvent);
    } catch (Exception ex) {
      LOGGER.log(
          Level.WARNING,
          "Custom events not supported for this browser, multia-pp support wont work. will inform local app listeners only");
      callListeners(token, title, stateJson);
    }
  }

  private NormalizedToken getNormalizedToken(String token, HistoryListener listener) {
    return listener.getTokenFilter().normalizeToken(token);
  }

  /**
   * Create a listener that will listen for any change in the browser url.
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
    HistoryListener historyListener = new HistoryListener(listener, tokenFilter, removeOnComplete);
    listeners.add(historyListener);
    return new DominoDirectState(tokenFilter, currentState(), listener)
        .onCompleted(
            dominoDirectState -> {
              if (historyListener.isRemoveOnComplete()) {
                listeners.remove(historyListener);
              }
            });
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

  /** Go back one step simulating the browser back button */
  @Override
  public void back() {
    history.back();
  }

  /** Go forward one step simulating the browser forward button */
  @Override
  public void forward() {
    history.forward();
  }

  /**
   * The count if the current browser history entries
   *
   * @return int
   */
  @Override
  public int getHistoryEntriesCount() {
    return new Double(history.length).intValue();
  }

  /**
   * Change the browser url to the specified token without firing url change listeners, sets the
   * title of the new page and assign the data to the new state.
   *
   * @param token The new browser url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   */
  @Override
  public void pushState(String token, String title, String data) {
    pushState(token, title, data, new TokenParameter[0]);
  }

  /**
   * Change the browser url to the specified token without firing url change listeners, sets the
   * title of the new page.
   *
   * @param token The new browser url.
   * @param title The new page title
   */
  @Override
  public void pushState(String token, String title) {
    pushState(token, title, "", new TokenParameter[0]);
  }

  /**
   * Change the browser url to the specified token without firing url change listeners, sets the
   * title of the new page and assign the data to the new state. In case the new token has
   * expression parameters in the form <b>:paramName</b> they will be replaced using the
   * <b>parameters</b>
   *
   * @param token The new browser url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  @Override
  public void pushState(String token, String title, String data, TokenParameter... parameters) {
    String tokenWithParameters = replaceParameters(token, Arrays.asList(parameters));
    if (nonNull(currentToken().value()) && !currentToken().value().equals(tokenWithParameters)) {
      history.pushState(
          JsState.state(tokenWithParameters, title, data), title, "/" + tokenWithParameters);
      if (nonNull(title) && !title.isEmpty()) {
        DomGlobal.document.title = title;
      }
    }
  }

  private String replaceParameters(String token, List<TokenParameter> parametersList) {
    String result = token;
    for (TokenParameter parameter : parametersList) {
      result = result.replace(":" + parameter.getName(), parameter.getValue());
    }
    return result;
  }

  /**
   * Change the browser url to the specified token and fire url change listeners.
   *
   * @param token The new browser url.
   */
  @Override
  public void fireState(String token) {
    fireState(token, new TokenParameter[0]);
  }

  /**
   * Change the browser url to the specified token and fire url change listeners. In case the new
   * token has expression parameters in the form <b>:paramName</b> they will be replaced using the
   * <b>parameters</b>
   *
   * @param token The new browser url.
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  @Override
  public void fireState(String token, TokenParameter... parameters) {
    pushState(token, parameters);
    fireCurrentStateHistory();
  }

  /**
   * Change the browser url to the specified token and fire change listeners, sets the title of the
   * new page and assign the data to the new state.
   *
   * @param token The new browser url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   */
  @Override
  public void fireState(String token, String title, String data) {
    fireState(token, title, data, new TokenParameter[0]);
  }

  /**
   * Change the browser url to the specified token and fire url change listeners, sets the title of
   * the new page and assign the data to the new state. In case the new token has expression
   * parameters in the form <b>:paramName</b> they will be replaced using the <b>parameters</b>
   *
   * @param token The new browser url.
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
   * Change the browser url to the specified token without firing url change listeners.
   *
   * @param token The new browser url.
   */
  @Override
  public void pushState(String token) {
    pushState(token, new TokenParameter[0]);
  }

  /**
   * Change the browser url to the specified token without firing url change listeners. In case the
   * new token has expression parameters in the form <b>:paramName</b> they will be replaced using
   * the <b>parameters</b>
   *
   * @param token The new browser url.
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  @Override
  public void pushState(String token, TokenParameter... parameters) {
    String tokenWithParameters = replaceParameters(token, Arrays.asList(parameters));
    if (nonNull(currentToken().value()) && !currentToken().value().equals(tokenWithParameters))
      history.pushState(
          JsState.state(tokenWithParameters, windowTitle(), ""),
          windowTitle(),
          "/" + tokenWithParameters);
  }

  /**
   * Replace the current browser url with the specified token without firing url change listeners,
   * sets the title of the new page and assign the data to the new state.
   *
   * @param token The new browser url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   */
  @Override
  public void replaceState(String token, String title, String data) {
    history.replaceState(JsState.state(token, data), title, "/" + token);
  }

  /**
   * Change the browser url to the specified token without firing url change listeners, sets the
   * title of the new page and assign the data to the new state.
   *
   * @param token {@link HistoryToken} The new browser url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   */
  @Override
  public void pushState(HistoryToken token, String title, String data) {
    pushState(token.value(), title, data);
  }

  /**
   * Change the browser url to the specified token without firing url change listeners, sets the
   * title of the new page and assign the data to the new state. In case the new token has
   * expression parameters in the form <b>:paramName</b> they will be replaced using the
   * <b>parameters</b>
   *
   * @param token {@link HistoryToken} The new browser url.
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
   * Change the browser url to the specified token without firing url change listeners.
   *
   * @param token {@link HistoryToken} The new browser url.
   */
  @Override
  public void pushState(HistoryToken token) {
    pushState(token.value());
  }

  /**
   * Change the browser url to the specified token without firing url change listeners. In case the
   * new token has expression parameters in the form <b>:paramName</b> they will be replaced using
   * the <b>parameters</b>
   *
   * @param token {@link HistoryToken} The new browser url.
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  @Override
  public void pushState(HistoryToken token, TokenParameter... parameters) {
    pushState(token.value(), parameters);
  }

  /**
   * Change the browser url to the specified token and fire change listeners, sets the title of the
   * new page and assign the data to the new state.
   *
   * @param token {@link HistoryToken} The new browser url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   */
  @Override
  public void fireState(HistoryToken token, String title, String data) {
    fireState(token.value(), title, data);
  }

  /**
   * Change the browser url to the specified token and fire url change listeners, sets the title of
   * the new page and assign the data to the new state. In case the new token has expression
   * parameters in the form <b>:paramName</b> they will be replaced using the <b>parameters</b>
   *
   * @param token {@link HistoryToken} The new browser url.
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
   * Change the browser url to the specified token and fire url change listeners.
   *
   * @param token {@link HistoryToken} The new browser url.
   */
  @Override
  public void fireState(HistoryToken token) {
    fireState(token.value());
  }

  /**
   * Change the browser url to the specified token and fire url change listeners. In case the new
   * token has expression parameters in the form <b>:paramName</b> they will be replaced using the
   * <b>parameters</b>
   *
   * @param token {@link HistoryToken} The new browser url.
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  @Override
  public void fireState(HistoryToken token, TokenParameter... parameters) {
    fireState(token.value(), parameters);
  }

  /**
   * Replace the current browser url with the specified token without firing url change listeners,
   * sets the title of the new page and assign the data to the new state.
   *
   * @param token {@link HistoryToken} The new browser url.
   * @param title The new page title
   * @param data The data to assign to this page state.
   */
  @Override
  public void replaceState(HistoryToken token, String title, String data) {
    replaceState(token.value(), title, data);
  }

  /**
   * Change the browser url to the specified token and fire the url change listeners, sets the title
   * of the new page.
   *
   * @param token The new browser url.
   * @param title The new page title
   */
  @Override
  public void fireState(String token, String title) {
    fireState(token, title, "");
  }

  /**
   * Change the browser url to the specified token without firing url change listeners, sets the
   * title of the new page.
   *
   * @param token {@link HistoryToken} The new browser url.
   * @param title The new page title
   */
  @Override
  public void pushState(HistoryToken token, String title) {
    pushState(token.value(), title, "");
  }

  /**
   * Change the browser url to the specified token and fire the url change listeners, sets the title
   * of the new page.
   *
   * @param token {@link HistoryToken} The new browser url.
   * @param title The new page title
   */
  @Override
  public void fireState(HistoryToken token, String title) {
    fireState(token.value(), title, "");
  }

  /**
   * Parse the current browser url and return an immutable instance of {@link StateHistoryToken}
   *
   * @return {@link StateHistoryToken}
   */
  @Override
  public StateHistoryToken currentToken() {
    return new StateHistoryToken(windowToken());
  }

  /**
   * Reapply the current token and browser url and force calling all listeners with matching token
   * filters.
   */
  @Override
  public void fireCurrentStateHistory() {
    fireStateInternal(windowToken(), windowTitle(), stateData(windowState()));
  }
  /**
   * Reapply the current token and browser url and force calling all listeners with matching token
   * filters. and use a new page title.
   *
   * @param title
   */
  @Override
  public void fireCurrentStateHistory(String title) {
    fireStateInternal(windowToken(), title, stateData(windowState()));
  }

  private void fireStateInternal(String token, String title, String state) {
    replaceState(token, title, state);
    inform(token, title, state);
  }

  private State windowState() {
    if (isNullState() || isNullToken()) {
      return nullState();
    }
    JsState jsState = getJsState();
    if (isNull(jsState)) {
      return new DominoHistoryState(windowToken(), windowTitle(), "");
    }
    return new DominoHistoryState(jsState.historyToken, jsState.title, jsState.data);
  }

  private boolean isNullToken() {
    JsState jsState = getJsState();
    return isNull(jsState.historyToken);
  }

  private JsState getJsState() {
    return Js.uncheckedCast(DomGlobal.self.history.state);
  }

  private boolean isNullState() {
    return isNull(DomGlobal.self.history.state);
  }

  private State nullState() {
    return new State() {
      @Override
      public HistoryToken token() {
        return new StateHistoryToken(windowToken());
      }

      @Override
      public Optional<String> data() {
        return Optional.empty();
      }

      @Override
      public String title() {
        return windowTitle();
      }

      @Override
      public NormalizedToken normalizedToken() {
        return new DefaultNormalizedToken(new StateHistoryToken(windowToken()));
      }

      @Override
      public void setNormalizedToken(NormalizedToken normalizedToken) {}
    };
  }

  private String windowTitle() {
    return DomGlobal.document.title;
  }

  private String windowToken() {
    Location location = Js.uncheckedCast(DomGlobal.location);
    return location.pathname.substring(1) + location.search + location.hash;
  }

  private State currentState() {
    return new DominoHistoryState(windowToken(), windowTitle(), stateData(windowState()));
  }

  private String stateData(State state) {
    return state.data().isPresent() ? state.data().get() : "";
  }

  private class DominoHistoryState implements State {

    private final HistoryToken token;
    private final String data;
    private final String title;
    private NormalizedToken normalizedToken;

    public DominoHistoryState(String token, String title, String data) {
      this.token = new StateHistoryToken(token);
      this.data = data;
      this.title = title;
      this.normalizedToken = new DefaultNormalizedToken(new StateHistoryToken(token));
    }

    public DominoHistoryState(
        NormalizedToken normalizedToken, String token, String title, String data) {
      this.token = new StateHistoryToken(token);
      this.data = data;
      this.title = title;
      this.normalizedToken = normalizedToken;
    }

    @Override
    public HistoryToken token() {
      return this.token;
    }

    @Override
    public Optional<String> data() {
      return Optional.ofNullable(this.data);
    }

    @Override
    public String title() {
      return this.title;
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
}
