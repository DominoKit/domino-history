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

public class TokenFilterToStringTest {

  @Test
  public void shouldDescribeTokenFilters() {
    assertThat(TokenFilter.any().toString()).isEqualTo("TokenFilter.any()");
    assertThat(TokenFilter.exactMatch("users/:id").toString())
        .isEqualTo("TokenFilter.exactMatch(\"users/:id\")");
    assertThat(TokenFilter.startsWith("users").toString())
        .isEqualTo("TokenFilter.startsWith(\"users\")");
    assertThat(TokenFilter.endsWith("details").toString())
        .isEqualTo("TokenFilter.endsWith(\"details\")");
    assertThat(TokenFilter.contains("admin").toString())
        .isEqualTo("TokenFilter.contains(\"admin\")");
  }

  @Test
  public void shouldDescribeFragmentAndPathFilters() {
    assertThat(TokenFilter.exactFragmentMatch("#section").toString())
        .isEqualTo("TokenFilter.exactFragmentMatch(\"#section\")");
    assertThat(TokenFilter.startsWithFragment("#section").toString())
        .isEqualTo("TokenFilter.startsWithFragment(\"#section\")");
    assertThat(TokenFilter.endsWithFragment("#details").toString())
        .isEqualTo("TokenFilter.endsWithFragment(\"#details\")");
    assertThat(TokenFilter.containsFragment("#tab").toString())
        .isEqualTo("TokenFilter.containsFragment(\"#tab\")");
    assertThat(TokenFilter.anyFragment().toString()).isEqualTo("TokenFilter.anyFragment()");

    assertThat(TokenFilter.hasPathFilter("users").toString())
        .isEqualTo("TokenFilter.hasPathFilter(\"users\")");
    assertThat(TokenFilter.hasPathsFilter("users", ":id").toString())
        .isEqualTo("TokenFilter.hasPathsFilter(\"users\", \":id\")");
    assertThat(TokenFilter.exactPathFilter("users/:id").toString())
        .isEqualTo("TokenFilter.exactPathFilter(\"users/:id\")");
    assertThat(TokenFilter.startsWithPathFilter("users").toString())
        .isEqualTo("TokenFilter.startsWithPathFilter(\"users\")");
    assertThat(TokenFilter.endsWithPathFilter(":id").toString())
        .isEqualTo("TokenFilter.endsWithPathFilter(\":id\")");
    assertThat(TokenFilter.anyPathFilter().toString()).isEqualTo("TokenFilter.anyPathFilter()");
    assertThat(TokenFilter.isEmpty().toString()).isEqualTo("TokenFilter.isEmpty()");
    assertThat(TokenFilter.queryParam("view", "summary").toString())
        .isEqualTo("TokenFilter.queryParam(\"view\", \"summary\")");
  }

  @Test
  public void shouldDescribeCompositeFilters() {
    TokenFilter exactMatch = TokenFilter.exactMatch("users/:id");
    TokenFilter startsWith = TokenFilter.startsWith("users");

    assertThat(TokenFilter.not(exactMatch).toString())
        .isEqualTo("TokenFilter.not(TokenFilter.exactMatch(\"users/:id\"))");
    assertThat(TokenFilter.and(exactMatch, startsWith).toString())
        .isEqualTo(
            "TokenFilter.and(TokenFilter.exactMatch(\"users/:id\"), TokenFilter.startsWith(\"users\"))");
    assertThat(TokenFilter.or(exactMatch, startsWith).toString())
        .isEqualTo(
            "TokenFilter.or(TokenFilter.exactMatch(\"users/:id\"), TokenFilter.startsWith(\"users\"))");
  }
}
