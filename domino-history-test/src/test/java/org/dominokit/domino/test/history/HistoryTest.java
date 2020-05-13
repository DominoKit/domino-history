package org.dominokit.domino.test.history;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HistoryTest {

    private TestDominoHistory testDominoHistory = new TestDominoHistory();

    //test for issue https://github.com/DominoKit/domino-history/issues/1
    @Test
    public void testBugReport() {
        testDominoHistory.pushState("be.foo.bar.App/index.html#config");
        testDominoHistory.fireCurrentStateHistory();
        assertThat(testDominoHistory.currentToken().value()).isEqualTo("be.foo.bar.App/index.html#config");
    }
}
