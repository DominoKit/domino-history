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

public interface CurrentStateHistory {

  /** Reapply the current token and force calling all listeners with matching token filters. */
  void fireCurrentStateHistory();

  /**
   * Reapply the current token and force calling all listeners with matching token filters. and use
   * a new page title.
   *
   * @param title the page title
   */
  void fireCurrentStateHistory(String title);
}
