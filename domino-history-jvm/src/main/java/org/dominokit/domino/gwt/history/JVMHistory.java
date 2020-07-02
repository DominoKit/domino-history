package org.dominokit.domino.gwt.history;

import org.dominokit.domino.history.*;

import java.util.*;

import static java.util.Objects.isNull;

public class JVMHistory implements AppHistory {

    private Set<HistoryListener> listeners = new HashSet<>();
    private Deque<HistoryState> forwards = new LinkedList<>();
    private Deque<HistoryState> backwards = new LinkedList<>();

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
        listeners.add(new HistoryListener(listener, tokenFilter, removeOnComplete));
        return new DominoDirectState(tokenFilter, currentState(), listener);
    }

    @Override
    public void removeListener(StateListener listener) {
        listeners.remove(listener);
    }

    private State currentState() {
        if (forwards.isEmpty())
            return new JVMState(nullState());
        return new JVMState(forwards.peek());
    }

    private HistoryState nullState() {
        return new HistoryState("", "");
    }

    private void inform(HistoryState state) {
        List<HistoryListener> completedListeners = new ArrayList<>();
        listeners.stream()
                .filter(l -> {
                    NormalizedToken normalized = getNormalizedToken(state.token, l);
                    if (isNull(normalized)) {
                        normalized = new DefaultNormalizedToken(state.token);
                    }
                    return l.tokenFilter.filter(new JVMState(new HistoryState(normalized.getToken().value(), "")).token());
                })
                .forEach(l -> {
                    if (l.isRemoveOnComplete()) {
                        completedListeners.add(l);
                    }

                    NormalizedToken normalized = getNormalizedToken(state.token, l);
                    l.listener.onPopState(new JVMState(normalized, new HistoryState(normalized.getToken().value(), "")));
                });

        listeners.removeAll(completedListeners);
    }

    private NormalizedToken getNormalizedToken(String token, HistoryListener listener) {
        return listener.tokenFilter.normalizeToken(token);
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
    public void pushState(String token, String title, String data) {
        push(token, data, new TokenParameter[0]);
    }

    @Override
    public void pushState(String token, String title, String data, TokenParameter... parameters) {
        push(token, data, parameters);
    }

    @Override
    public void pushState(String token) {
        push(token, "", new TokenParameter[0]);
    }

    @Override
    public void pushState(String token, TokenParameter... parameters) {
        push(token, "", parameters);
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
    public void fireState(String token) {
        fireState(token, new TokenParameter[0]);
    }

    @Override
    public void fireState(String token, TokenParameter... parameters) {
        pushState(token, parameters);
        fireCurrentStateHistory();
    }

    @Override
    public void replaceState(String token, String title, String data) {
        forwards.pop();
        push(token, data);
    }

    @Override
    public void pushState(HistoryToken token, String title, String data) {
        pushState(token.value(), title, data);
    }

    @Override
    public void pushState(HistoryToken token, String title, String data, TokenParameter... parameters) {
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
    public void fireState(HistoryToken token, String title, String data, TokenParameter... parameters) {
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
    public void pushState(String token, String title) {
        pushState(token, title, "");
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
    public HistoryToken currentToken() {
        if (isNull(forwards.peek()))
            return new StateHistoryToken("");
        return new StateHistoryToken(forwards.peek().token);
    }

    @Override
    public void fireCurrentStateHistory() {
        if (!forwards.isEmpty())
            inform(forwards.peek());
    }

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

        public HistoryListener(StateListener listener, TokenFilter tokenFilter, boolean removeOnComplete) {
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
        public HistoryToken token() {
            return new StateHistoryToken(historyState.token);
        }

        @Override
        public String data() {
            return historyState.data;
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