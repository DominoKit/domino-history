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
  private String rootPath;

  private final List<HistoryInterceptor> interceptors = new ArrayList<>();

  /** Default constructor */
  public StateHistory() {
    this("");
  }

  /** Default constructor */
  public StateHistory(String rootPath) {
    setRootPath(rootPath);
    DomGlobal.self.addEventListener(
        "popstate",
        event -> {
          if (isInformOnPopState()) {
            PopStateEvent popStateEvent = Js.cast(event);
            JsState state = Js.cast(popStateEvent.state);
            if (nonNull(state) && nonNull(state.historyToken)) {
              inform(
                  new EffectiveToken(
                      "", StateToken.of(state.historyToken).title(state.title).data(state.data)));
            } else {
              inform(
                  new EffectiveToken(
                      "", StateToken.of(windowToken()).title(windowTitle()).data("")));
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
          callListeners(new EffectiveToken("", StateToken.of(token).title(title).data(stateJson)));
        });
  }

  private void callListeners(EffectiveToken effectiveToken) {
    if (!isSameRoot(effectiveToken.getToken())) {
      return;
    }
    final List<HistoryListener> completedListeners = new ArrayList<>();
    listeners.stream()
        .filter(
            listener -> {
              NormalizedToken normalized =
                  getNormalizedToken(rootPath, effectiveToken.getToken(), listener);
              if (isNull(normalized)) {
                normalized = new DefaultNormalizedToken(rootPath, effectiveToken.getToken());
              }
              return listener
                  .getTokenFilter()
                  .filter(
                      new DominoHistoryState(
                              normalized.getToken().value(),
                              effectiveToken.getTitle(),
                              effectiveToken.getData())
                          .token);
            })
        .forEach(
            listener -> {
              if (listener.isRemoveOnComplete()) {
                completedListeners.add(listener);
              }
              DomGlobal.setTimeout(
                  p0 -> {
                    NormalizedToken normalized =
                        getNormalizedToken(rootPath, effectiveToken.getToken(), listener);
                    listener
                        .getListener()
                        .onPopState(
                            new DominoHistoryState(
                                normalized,
                                effectiveToken.getToken(),
                                effectiveToken.getTitle(),
                                effectiveToken.getData()));
                  },
                  0);
            });

    listeners.removeAll(completedListeners);
  }

  private void inform(EffectiveToken effectiveToken) {

    if (!isSameRoot(effectiveToken.getToken())) {
      return;
    }

    try {
      JsMap<String, String> tokenMap = new JsMap<>();
      tokenMap.set("token", effectiveToken.getToken());
      tokenMap.set("title", effectiveToken.getTitle());
      tokenMap.set("stateJson", effectiveToken.getData());

      CustomEventInit tokenEventInit = CustomEventInit.create();
      tokenEventInit.setDetail(tokenMap);

      CustomEvent tokenEvent = new CustomEvent("domino-history-event", tokenEventInit);
      DomGlobal.document.dispatchEvent(tokenEvent);
    } catch (Exception ex) {
      LOGGER.log(
          Level.WARNING,
          "Custom events not supported for this browser, multi-app support wont work. will inform local app listeners only");
      callListeners(effectiveToken);
    }
  }

  private NormalizedToken getNormalizedToken(
      String rootPath, String token, HistoryListener listener) {
    return listener.getTokenFilter().normalizeToken(rootPath, token);
  }

  private boolean isSameRoot(String token) {
    if (this.rootPath.isEmpty()) {
      return true;
    }
    return token.startsWith(rootPath);
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
   * title of the new page and assign the data to the new state. In case the new token has
   * expression parameters in the form <b>:paramName</b> they will be replaced using the
   * <b>parameters</b>
   *
   * @param stateToken {@link StateToken}.
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  @Override
  public void pushState(StateToken stateToken, TokenParameter... parameters) {
    pushState(stateToken, () -> {}, parameters);
  }

  private void pushState(
      StateToken stateToken, Runnable onPushHandler, TokenParameter... parameters) {
    InterceptorChain interceptorChain =
        new InterceptorChain(
            interceptors,
            () -> {
              EffectiveToken effectiveToken = new EffectiveToken(rootPath, stateToken, parameters);
              if (nonNull(currentToken().value())
                  && !currentToken().value().equals(effectiveToken.getToken())) {
                history.pushState(
                    JsState.state(effectiveToken),
                    Optional.ofNullable(effectiveToken.getTitle()).orElse(windowTitle()),
                    "/" + effectiveToken.getToken());
                setPageTitle(effectiveToken);
                onPushHandler.run();
              }
            });
    interceptorChain.intercept(new TokenEvent(rootPath, stateToken));
  }

  private static void setPageTitle(EffectiveToken effectiveToken) {
    if (nonNull(effectiveToken.getTitle()) && !effectiveToken.getTitle().isEmpty()) {
      DomGlobal.document.title = effectiveToken.getTitle();
    }
  }

  /**
   * Change the browser url to the specified token and fire url change listeners.
   *
   * @param token The new browser url.
   */
  @Override
  public void fireState(StateToken token) {
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
  public void fireState(StateToken token, TokenParameter... parameters) {
    pushState(token, this::fireCurrentStateHistory, parameters);
  }

  /**
   * Change the browser url to the specified token without firing url change listeners.
   *
   * @param token The new browser url.
   */
  @Override
  public void pushState(StateToken token) {
    pushState(token, new TokenParameter[0]);
  }

  /**
   * Replace the current browser url with the specified token without firing url change listeners,
   * sets the title of the new page and assign the data to the new state.
   *
   * @param stateToken {@link StateToken}.
   */
  @Override
  public void replaceState(StateToken stateToken) {
    replaceState(new EffectiveToken(rootPath, stateToken));
  }

  /**
   * Replace the current browser url with the specified token without firing url change listeners,
   * sets the title of the new page and assign the data to the new state.
   *
   * @param effectiveToken {@link EffectiveToken}.
   */
  private void replaceState(EffectiveToken effectiveToken) {
    history.replaceState(
        JsState.state(effectiveToken),
        Optional.ofNullable(effectiveToken.getTitle()).orElse(windowTitle()),
        "/" + effectiveToken.getToken());
    setPageTitle(effectiveToken);
  }

  /**
   * Parse the current browser url and return an immutable instance of {@link StateHistoryToken}
   *
   * @return {@link StateHistoryToken}
   */
  @Override
  public StateHistoryToken currentToken() {
    return new StateHistoryToken(rootPath, windowToken());
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

  /**
   * Reapply the current token and browser url and force calling all listeners with matching token
   * filters.
   */
  @Override
  public void fireCurrentStateHistory() {
    fireCurrentStateHistory(windowTitle());
  }

  /** Invoke the listeners with the current token without firing an event */
  @Override
  public void invoke() {
    StateToken stateToken =
        StateToken.of(windowToken()).title(windowTitle()).data(stateData(windowState()));
    EffectiveToken effectiveToken = new EffectiveToken(rootPath, stateToken);
    callListeners(effectiveToken);
  }

  /**
   * Reapply the current token and browser url and force calling all listeners with matching token
   * filters. and use a new page title.
   *
   * @param title The page title
   */
  @Override
  public void fireCurrentStateHistory(String title) {
    fireStateInternal(StateToken.of(windowToken()).title(title).data(stateData(windowState())));
  }

  @Override
  public void reload() {
    Location location = Js.uncheckedCast(DomGlobal.location);
    location.reload();
  }

  private void fireStateInternal(StateToken stateToken) {
    EffectiveToken effectiveToken = new EffectiveToken(rootPath, stateToken);
    replaceState(effectiveToken);
    inform(effectiveToken);
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
      public String rootPath() {
        return StateHistory.this.rootPath;
      }

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
      this.token = new StateHistoryToken(rootPath, token);
      this.data = data;
      this.title = title;
      this.normalizedToken = new DefaultNormalizedToken(new StateHistoryToken(rootPath, token));
    }

    public DominoHistoryState(
        NormalizedToken normalizedToken, String token, String title, String data) {
      this.token = new StateHistoryToken(rootPath, token);
      this.data = data;
      this.title = title;
      this.normalizedToken = normalizedToken;
    }

    @Override
    public String rootPath() {
      return StateHistory.this.rootPath;
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
