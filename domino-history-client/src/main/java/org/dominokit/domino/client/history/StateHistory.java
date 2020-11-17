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
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import elemental2.dom.PopStateEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsinterop.base.Js;
import org.dominokit.domino.history.*;

public class StateHistory implements AppHistory {

  private static final Logger LOGGER = Logger.getLogger(StateHistory.class.getName());

  private Set<HistoryListener> listeners = new HashSet<>();
  private final History history = Js.cast(DomGlobal.self.history);

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

  @Override
  public void removeListener(StateListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void back() {
    history.back();
  }

  @Override
  public void forward() {
    history.forward();
  }

  @Override
  public int getHistoryEntriesCount() {
    return new Double(history.getLength()).intValue();
  }

  @Override
  public void pushState(String token, String title, String data) {
    pushState(token, title, data, new TokenParameter[0]);
  }

  @Override
  public void pushState(String token, String title) {
    pushState(token, title, "", new TokenParameter[0]);
  }

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

  @Override
  public void fireState(String token) {
    fireState(token, new TokenParameter[0]);
  }

  @Override
  public void fireState(String token, TokenParameter... parameters) {
    pushState(token, parameters);
    fireCurrentStateHistory();
  }

  @Override
  public void fireState(String token, String title, String data) {
    fireState(token, title, data, new TokenParameter[0]);
  }

  @Override
  public void fireState(String token, String title, String data, TokenParameter... parameters) {
    pushState(token, title, data, parameters);
    fireCurrentStateHistory();
  }

  @Override
  public void pushState(String token) {
    pushState(token, new TokenParameter[0]);
  }

  @Override
  public void pushState(String token, TokenParameter... parameters) {
    String tokenWithParameters = replaceParameters(token, Arrays.asList(parameters));
    if (nonNull(currentToken().value()) && !currentToken().value().equals(tokenWithParameters))
      history.pushState(
          JsState.state(tokenWithParameters, windowTitle(), ""),
          windowTitle(),
          "/" + tokenWithParameters);
  }

  @Override
  public void replaceState(String token, String title, String data) {
    history.replaceState(JsState.state(token, data), title, "/" + token);
  }

  @Override
  public void pushState(HistoryToken token, String title, String data) {
    pushState(token.value(), title, data);
  }

  @Override
  public void pushState(
      HistoryToken token, String title, String data, TokenParameter... parameters) {
    pushState(token.value(), title, data, parameters);
  }

  @Override
  public void pushState(HistoryToken token) {
    pushState(token.value());
  }

  @Override
  public void pushState(HistoryToken token, TokenParameter... parameters) {
    pushState(token.value(), parameters);
  }

  @Override
  public void fireState(HistoryToken token, String title, String data) {
    fireState(token.value(), title, data);
  }

  @Override
  public void fireState(
      HistoryToken token, String title, String data, TokenParameter... parameters) {
    fireState(token.value(), title, data, parameters);
  }

  @Override
  public void fireState(HistoryToken token) {
    fireState(token.value());
  }

  @Override
  public void fireState(HistoryToken token, TokenParameter... parameters) {
    fireState(token.value(), parameters);
  }

  @Override
  public void replaceState(HistoryToken token, String title, String data) {
    replaceState(token.value(), title, data);
  }

  @Override
  public void fireState(String token, String title) {
    fireState(token, title, "");
  }

  @Override
  public void pushState(HistoryToken token, String title) {
    pushState(token.value(), title, "");
  }

  @Override
  public void fireState(HistoryToken token, String title) {
    fireState(token.value(), title, "");
  }

  @Override
  public StateHistoryToken currentToken() {
    return new StateHistoryToken(windowToken());
  }

  @Override
  public void fireCurrentStateHistory() {
    fireStateInternal(windowToken(), windowTitle(), stateData(windowState()));
  }

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
      public String data() {
        return "";
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
    return location.getPathname().substring(1) + location.getSearch() + location.getHash();
  }

  private State currentState() {
    return new DominoHistoryState(windowToken(), windowTitle(), stateData(windowState()));
  }

  private String stateData(State state) {
    return isNull(state) ? "" : state.data();
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
    public String data() {
      return this.data;
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
