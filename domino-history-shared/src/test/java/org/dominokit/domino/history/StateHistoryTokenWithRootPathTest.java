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
package org.dominokit.domino.history;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StateHistoryTokenWithRootPathTest {

  private StateHistoryToken token(String token) {
    return new StateHistoryToken(token);
  }

  private StateHistoryToken token(String rootPath, String token) {
    return new StateHistoryToken(rootPath, token);
  }

  private StateHistoryToken tokenWithRoot(String token) {
    return new StateHistoryToken("app/ui", token);
  }

  @Test(expected = HistoryToken.TokenCannotBeNullException.class)
  public void nullToken() {
    token(null);
  }

  @Test
  public void noRootPath() {
    StateHistoryToken token = token("app/ui");
    assertThat(token.getRootPath()).isEmpty();
  }

  @Test
  public void nullRootPath() {
    StateHistoryToken token = token(null, "path1/path2");
    assertThat(token.getRootPath()).isEmpty();
  }

  @Test
  public void emptyToken() {
    StateHistoryToken token = tokenWithRoot("");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui");
  }

  @Test
  public void singleSlashToken() {
    StateHistoryToken token = tokenWithRoot("/");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui");
  }

  @Test
  public void doubleSlashToken() {
    StateHistoryToken token = tokenWithRoot("//");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui");
  }

  @Test
  public void duplicateTokenRootToken() {
    StateHistoryToken token = tokenWithRoot("app/ui");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui");
  }

  @Test
  public void pathTest() {
    StateHistoryToken token = tokenWithRoot("path1");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1");

    token = tokenWithRoot("/path1");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1");

    token = tokenWithRoot("path1/path2");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1/path2");
    assertThat(token.paths().size()).isEqualTo(2);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.paths().get(1)).isEqualTo("path2");

    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1/path2");

    token = tokenWithRoot("path1/path2/path3");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1/path2/path3");
    assertThat(token.paths().size()).isEqualTo(3);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.paths().get(1)).isEqualTo("path2");
    assertThat(token.paths().get(2)).isEqualTo("path3");

    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1/path2/path3");

    token = tokenWithRoot("path1/path2/path1");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1/path2/path1");
    assertThat(token.paths().size()).isEqualTo(3);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.paths().get(1)).isEqualTo("path2");
    assertThat(token.paths().get(2)).isEqualTo("path1");

    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1/path2/path1");
  }

  @Test
  public void queryTest() {
    StateHistoryToken token = tokenWithRoot("?");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui");

    token = tokenWithRoot("?param1=value1");
    assertThat(token.query()).isEqualTo("param1=value1");
    assertThat(token.queryParameters().size()).isEqualTo(1);
    assertThat(token.getQueryParameter("param1")).containsOnly("value1");
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui?param1=value1");

    token = tokenWithRoot("?param1=value1&param2=value2");
    assertThat(token.query()).isEqualTo("param1=value1&param2=value2");
    assertThat(token.queryParameters().size()).isEqualTo(2);
    assertThat(token.getQueryParameter("param1")).containsOnly("value1");
    assertThat(token.getQueryParameter("param2")).containsOnly("value2");
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui?param1=value1&param2=value2");

    token = tokenWithRoot("?param1=value1&param2=value2&param3=value1");
    assertThat(token.query()).isEqualTo("param1=value1&param2=value2&param3=value1");
    assertThat(token.queryParameters().size()).isEqualTo(3);
    assertThat(token.getQueryParameter("param1")).containsOnly("value1");
    assertThat(token.getQueryParameter("param2")).containsOnly("value2");
    assertThat(token.getQueryParameter("param3")).containsOnly("value1");
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui?param1=value1&param2=value2&param3=value1");
  }

  @Test
  public void fragmentTest() {
    StateHistoryToken token = tokenWithRoot("#");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui");

    token = tokenWithRoot("#path1");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEqualTo("path1");
    assertThat(token.fragments().size()).isEqualTo(1);
    assertThat(token.fragments().get(0)).isEqualTo("path1");
    assertThat(token.value()).isEqualTo("app/ui#path1");

    token = tokenWithRoot("#path1/path2");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEqualTo("path1/path2");
    assertThat(token.fragments().size()).isEqualTo(2);
    assertThat(token.fragments().get(0)).isEqualTo("path1");
    assertThat(token.fragments().get(1)).isEqualTo("path2");
    assertThat(token.value()).isEqualTo("app/ui#path1/path2");

    token = tokenWithRoot("#path1/path2/path1");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEqualTo("path1/path2/path1");
    assertThat(token.fragments().size()).isEqualTo(3);
    assertThat(token.fragments().get(0)).isEqualTo("path1");
    assertThat(token.fragments().get(1)).isEqualTo("path2");
    assertThat(token.fragments().get(2)).isEqualTo("path1");
    assertThat(token.value()).isEqualTo("app/ui#path1/path2/path1");
  }

  @Test
  public void pathAndQueryTest() {
    StateHistoryToken token = tokenWithRoot("/?");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui");

    token = tokenWithRoot("path1?");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1");

    token = tokenWithRoot("/path1?");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1");

    token = tokenWithRoot("path1/?");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1");

    token = tokenWithRoot("/path1/?");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1");

    token = tokenWithRoot("path1?param1=value1");
    assertThat(token.query()).isEqualTo("param1=value1");
    assertThat(token.queryParameters().size()).isEqualTo(1);
    assertThat(token.getQueryParameter("param1")).containsOnly("value1");
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1?param1=value1");

    token = tokenWithRoot("path1/path2?param1=value1&param2=value2");
    assertThat(token.query()).isEqualTo("param1=value1&param2=value2");
    assertThat(token.queryParameters().size()).isEqualTo(2);
    assertThat(token.getQueryParameter("param1")).containsOnly("value1");
    assertThat(token.getQueryParameter("param2")).containsOnly("value2");
    assertThat(token.path()).isEqualTo("path1/path2");
    assertThat(token.paths().size()).isEqualTo(2);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.paths().get(1)).isEqualTo("path2");
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1/path2?param1=value1&param2=value2");
  }

  @Test
  public void pathAndFragmentTest() {
    StateHistoryToken token = tokenWithRoot("/#");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui");

    token = tokenWithRoot("path1#");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1");

    token = tokenWithRoot("/path1#");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1");

    token = tokenWithRoot("path1/#");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1");

    token = tokenWithRoot("/path1/#");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1");

    token = tokenWithRoot("path1#fragment1");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.fragment()).isEqualTo("fragment1");
    assertThat(token.fragments().size()).isEqualTo(1);
    assertThat(token.fragments().get(0)).isEqualTo("fragment1");
    assertThat(token.value()).isEqualTo("app/ui/path1#fragment1");

    token = tokenWithRoot("path1/path2#fragment1/fragment2");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1/path2");
    assertThat(token.paths().size()).isEqualTo(2);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.paths().get(1)).isEqualTo("path2");
    assertThat(token.fragment()).isEqualTo("fragment1/fragment2");
    assertThat(token.fragments().size()).isEqualTo(2);
    assertThat(token.fragments().get(0)).isEqualTo("fragment1");
    assertThat(token.fragments().get(1)).isEqualTo("fragment2");
    assertThat(token.value()).isEqualTo("app/ui/path1/path2#fragment1/fragment2");
  }

  @Test
  public void queryAndFragmentTest() {
    StateHistoryToken token = tokenWithRoot("?#");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui");

    token = tokenWithRoot("?#fragment1");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEqualTo("fragment1");
    assertThat(token.fragments().size()).isEqualTo(1);
    assertThat(token.fragments().get(0)).isEqualTo("fragment1");
    assertThat(token.value()).isEqualTo("app/ui#fragment1");

    token = tokenWithRoot("?param1=value1#");
    assertThat(token.query()).isEqualTo("param1=value1");
    assertThat(token.queryParameters().size()).isEqualTo(1);
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui?param1=value1");

    token = tokenWithRoot("?param1=value1#fragment1");
    assertThat(token.query()).isEqualTo("param1=value1");
    assertThat(token.queryParameters().size()).isEqualTo(1);
    assertThat(token.getQueryParameter("param1")).containsOnly("value1");
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEqualTo("fragment1");
    assertThat(token.fragments().size()).isEqualTo(1);
    assertThat(token.fragments().get(0)).isEqualTo("fragment1");
    assertThat(token.value()).isEqualTo("app/ui?param1=value1#fragment1");

    token = tokenWithRoot("?param1=value1&param2=value2#fragment1/fragment2");
    assertThat(token.query()).isEqualTo("param1=value1&param2=value2");
    assertThat(token.queryParameters().size()).isEqualTo(2);
    assertThat(token.getQueryParameter("param1")).containsOnly("value1");
    assertThat(token.getQueryParameter("param2")).containsOnly("value2");
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEqualTo("fragment1/fragment2");
    assertThat(token.fragments().size()).isEqualTo(2);
    assertThat(token.fragments().get(0)).isEqualTo("fragment1");
    assertThat(token.fragments().get(1)).isEqualTo("fragment2");
    assertThat(token.value()).isEqualTo("app/ui?param1=value1&param2=value2#fragment1/fragment2");
  }

  @Test
  public void pathAndQueryAndFragmentTest() {
    StateHistoryToken token = tokenWithRoot("/?#");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui");

    token = tokenWithRoot("/?#fragment1");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEqualTo("fragment1");
    assertThat(token.fragments().size()).isEqualTo(1);
    assertThat(token.fragments().get(0)).isEqualTo("fragment1");
    assertThat(token.value()).isEqualTo("app/ui#fragment1");

    token = tokenWithRoot("/?param1=value1#");
    assertThat(token.query()).isEqualTo("param1=value1");
    assertThat(token.queryParameters().size()).isEqualTo(1);
    assertThat(token.getQueryParameter("param1")).containsOnly("value1");
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui?param1=value1");

    token = tokenWithRoot("/path1?#");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1");

    token = tokenWithRoot("/path1?param1=value1#");
    assertThat(token.query()).isEqualTo("param1=value1");
    assertThat(token.queryParameters().size()).isEqualTo(1);
    assertThat(token.getQueryParameter("param1")).containsOnly("value1");
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.fragment()).isEmpty();
    assertThat(token.fragments()).isEmpty();
    assertThat(token.value()).isEqualTo("app/ui/path1?param1=value1");

    token = tokenWithRoot("/path1?#fragment1");
    assertThat(token.query()).isEmpty();
    assertThat(token.queryParameters()).isEmpty();
    assertThat(token.path()).isEqualTo("path1");
    assertThat(token.paths().size()).isEqualTo(1);
    assertThat(token.paths().get(0)).isEqualTo("path1");
    assertThat(token.fragment()).isEqualTo("fragment1");
    assertThat(token.fragments().size()).isEqualTo(1);
    assertThat(token.fragments().get(0)).isEqualTo("fragment1");
    assertThat(token.value()).isEqualTo("app/ui/path1#fragment1");

    token = tokenWithRoot("/?param1=value1#fragment1");
    assertThat(token.query()).isEqualTo("param1=value1");
    assertThat(token.queryParameters().size()).isEqualTo(1);
    assertThat(token.getQueryParameter("param1")).containsOnly("value1");
    assertThat(token.path()).isEmpty();
    assertThat(token.paths()).isEmpty();
    assertThat(token.fragment()).isEqualTo("fragment1");
    assertThat(token.fragments().size()).isEqualTo(1);
    assertThat(token.fragments().get(0)).isEqualTo("fragment1");
    assertThat(token.value()).isEqualTo("app/ui?param1=value1#fragment1");
  }

  @Test(expected = HistoryToken.InvalidQueryStringException.class)
  public void invalidQueryParamTest() {
    tokenWithRoot("?param1");
  }

  @Test
  public void issueTest() {
    StateHistoryToken token = tokenWithRoot("/path1/path2?employeeId=129");
    assertThat(token.query()).isEqualTo("employeeId=129");
    assertThat(token.queryParameters().size()).isEqualTo(1);
    assertThat(token.getQueryParameter("employeeId")).containsOnly("129");
  }
}
