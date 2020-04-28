package org.dominokit.domino.test.history;

import org.junit.Test;

public class HistoryTest {

    private TestDominoHistory testDominoHistory = new TestDominoHistory();

    @Test
    public void testBugReport(){
//        http://127.0.0.1:8888/be.foo.bar.App/index.html#config

        testDominoHistory.pushState("be.foo.bar.App/index.html?#config");
        testDominoHistory.fireCurrentStateHistory();
        System.out.println(testDominoHistory.currentToken().value());
    }
}
