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
package org.dominokit.domino.test.history;

import static org.assertj.core.api.Assertions.assertThat;

import org.dominokit.domino.history.StateToken;
import org.dominokit.domino.history.TokenUtil;
import org.junit.Test;

public class HistoryTest {

  private TestDominoHistory testDominoHistory = new TestDominoHistory();

  // test for issue https://github.com/DominoKit/domino-history/issues/1
  @Test
  public void testBugReport() {
    testDominoHistory.pushState(StateToken.of("be.foo.bar.App/index.html#config"));
    testDominoHistory.fireCurrentStateHistory();
    assertThat(testDominoHistory.currentToken().value())
        .isEqualTo("be.foo.bar.App/index.html#config");
  }

  @Test
  public void testUnHashFragment() {
    assertThat(TokenUtil.unHashFragment("fragment1/fragment2")).isEqualTo("fragment1/fragment2");
    assertThat(TokenUtil.unHashFragment("#")).isEqualTo("");
    assertThat(TokenUtil.unHashFragment("###")).isEqualTo("");
    assertThat(TokenUtil.unHashFragment("")).isEqualTo("");
    assertThat(TokenUtil.unHashFragment("#fragment1/fragment2")).isEqualTo("fragment1/fragment2");
    assertThat(TokenUtil.unHashFragment("fragment1#fragment2")).isEqualTo("fragment2");
    assertThat(TokenUtil.unHashFragment("fragment1/fragment2#")).isEqualTo("");
    assertThat(TokenUtil.unHashFragment("fragment1/#fragment2#")).isEqualTo("");
    assertThat(TokenUtil.unHashFragment("fragment1/#fragment2")).isEqualTo("fragment2");
    assertThat(TokenUtil.unHashFragment("#fragment1/#fragment2")).isEqualTo("fragment2");
  }
}
