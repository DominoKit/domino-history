package org.dominokit.domino.history;

public interface DominoHistory {

    DirectState listen(StateListener listener);

    void removeListener(StateListener listener);

    DirectState listen(TokenFilter tokenFilter, StateListener listener);

    DirectState listen(StateListener listener, boolean removeOnComplete);

    DirectState listen(TokenFilter tokenFilter, StateListener listener, boolean removeOnComplete);

    default boolean isInformOnPopState() {
        return true;
    }

    void back();

    void forward();

    int getHistoryEntriesCount();

    void pushState(String token, String title, String data);

    void pushState(String token, String title);

    void pushState(String token, String title, String data, TokenParameter... parameters);

    void pushState(String token);

    void pushState(String token, TokenParameter... parameters);

    void fireState(String token, String title, String data);

    void fireState(String token, String title);

    void fireState(String token, String title, String data, TokenParameter... parameters);

    void fireState(String token);

    void fireState(String token, TokenParameter... parameters);

    void replaceState(String token, String title, String data);

    void pushState(HistoryToken token, String title, String data);

    void pushState(HistoryToken token, String title);

    void pushState(HistoryToken token, String title, String data, TokenParameter... parameters);

    void pushState(HistoryToken token);

    void pushState(HistoryToken token, TokenParameter... parameters);

    void fireState(HistoryToken token, String title, String data);

    void fireState(HistoryToken token, String title);

    void fireState(HistoryToken token, String title, String data, TokenParameter... parameters);

    void fireState(HistoryToken token);

    void fireState(HistoryToken token, TokenParameter... parameters);

    void replaceState(HistoryToken token, String title, String data);

    HistoryToken currentToken();

    interface StateListener {
        void onPopState(State state);
    }

    interface State {
        HistoryToken token();

        String data();

        String title();

        NormalizedToken normalizedToken();

        void setNormalizedToken(NormalizedToken normalizedToken);
    }

    @FunctionalInterface
    interface DirectUrlHandler {
        void handle(State state);
    }

    interface DirectState {
        void onDirectUrl();

        void onDirectUrl(TokenFilter tokenFilter);
    }
}
