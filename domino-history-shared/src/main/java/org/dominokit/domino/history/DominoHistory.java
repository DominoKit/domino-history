package org.dominokit.domino.history;

public interface DominoHistory {

    DirectState listen(StateListener listener);

    void removeListener(StateListener listener);

    DirectState listen(TokenFilter tokenFilter, StateListener listener);

    DirectState listen(StateListener listener, boolean removeOnComplete);

    DirectState listen(TokenFilter tokenFilter, StateListener listener, boolean removeOnComplete);

    void back();

    void forward();

    int getHistoryEntriesCount();

    void pushState(String token, String title, String data);
    void pushState(String token, String title, String data, TokenParameter... parameters);
    void pushState(String token);
    void pushState(String token, TokenParameter... parameters);
    void fireState(String token, String title, String data);
    void fireState(String token, String title, String data, TokenParameter... parameters);
    void fireState(String token);
    void fireState(String token, TokenParameter... parameters);

    void replaceState(String token, String title, String data);

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

    interface DirectState{
        void onDirectUrl();
        void onDirectUrl(TokenFilter tokenFilter);
    }
}
