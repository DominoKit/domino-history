package org.dominokit.domino.history;

public interface CurrentStateHistory {
    void fireCurrentStateHistory();
    void fireCurrentStateHistory(String title);
}
