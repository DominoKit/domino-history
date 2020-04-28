package org.dominokit.domino.gwt.history;

import elemental2.core.JsMap;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import elemental2.dom.PopStateEvent;
import jsinterop.base.Js;
import org.dominokit.domino.history.*;
import org.gwtproject.core.client.Scheduler;

import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class StateHistory implements AppHistory {

    private Set<HistoryListener> listeners = new HashSet<>();
    private final History history = Js.cast(DomGlobal.self.history);

    public StateHistory() {
        DomGlobal.self.addEventListener("popstate", event -> {
            PopStateEvent popStateEvent = Js.cast(event);
            JsState state = Js.cast(popStateEvent.state);
            if (nonNull(state) && nonNull(state.historyToken)) {
                inform(state.historyToken, state.title, state.data);
            } else {
                inform(windowToken(), windowTitle(), "");
            }
        });

        DomGlobal.document.addEventListener("domino-history-event", evt -> {

            CustomEvent tokenEvent =Js.uncheckedCast(evt);
            JsMap<String, String> tokenMap = Js.uncheckedCast(tokenEvent.detail);
            final List<HistoryListener> completedListeners = new ArrayList<>();
            String token = tokenMap.get("token");
            String title = tokenMap.get("title");
            String stateJson = tokenMap.get("stateJson");
            listeners.stream()
                    .filter(l -> {
                        NormalizedToken normalized = getNormalizedToken(token, l);
                        if (isNull(normalized)) {
                            normalized = new DefaultNormalizedToken(token);
                        }
                        return l.getTokenFilter().filter(new DominoHistoryState(normalized.getToken().value(), title, stateJson).token);
                    })
                    .forEach(l -> {
                        if (l.isRemoveOnComplete()) {
                            completedListeners.add(l);
                        }

                        Scheduler.get()
                                .scheduleDeferred(() -> {
                                    NormalizedToken normalized = getNormalizedToken(token, l);
                                    l.getListener().onPopState(new DominoHistoryState(normalized, token, title, stateJson));
                                });
                    });

            listeners.removeAll(completedListeners);
        });
    }

    private void inform(String token, String title, String stateJson) {
        JsMap<String, String> tokenMap = new JsMap<>();
        tokenMap.set("token", token);
        tokenMap.set("title", title);
        tokenMap.set("stateJson", stateJson);

        CustomEventInit tokenEventInit=CustomEventInit.create();
        tokenEventInit.setDetail(tokenMap);

        CustomEvent tokenEvent= new CustomEvent("domino-history-event", tokenEventInit);
        DomGlobal.document.dispatchEvent(tokenEvent);
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
    public DirectState listen(TokenFilter tokenFilter, StateListener listener, boolean removeOnComplete) {
        HistoryListener historyListener = new HistoryListener(listener, tokenFilter, removeOnComplete);
        listeners.add(historyListener);
        return new DominoDirectState(tokenFilter, currentState(), listener)
                .onCompleted(dominoDirectState -> {
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
    public void pushState(String token, String title, String data) {
        pushState(token, title, data, new TokenParameter[0]);
    }

    @Override
    public void pushState(String token, String title, String data, TokenParameter... parameters) {
        String tokenWithParameters = replaceParameters(token, Arrays.asList(parameters));
        if (nonNull(currentToken().value()) && !currentToken().value().equals(tokenWithParameters))
            history.pushState(JsState.state(tokenWithParameters, title, data), title, "/" + tokenWithParameters);
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
            history.pushState(JsState.state(tokenWithParameters, windowTitle(), ""), windowTitle(), "/" + tokenWithParameters);
    }

    @Override
    public void replaceState(String token, String title, String data) {
        history.replaceState(
                JsState.state(token, data), title, "/" + token);
    }

    @Override
    public StateHistoryToken currentToken() {
        return new StateHistoryToken(windowToken());
    }

    @Override
    public void fireCurrentStateHistory() {
        fireState(windowToken(), stateData(windowState()));
    }

    private void fireState(String token, String state) {
        replaceState(token, windowTitle(), state);
        inform(token, windowTitle(), state);
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
            public void setNormalizedToken(NormalizedToken normalizedToken) {
            }
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

        public DominoHistoryState(NormalizedToken normalizedToken, String token, String title, String data) {
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
