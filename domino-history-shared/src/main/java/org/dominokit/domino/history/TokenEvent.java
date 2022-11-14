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

public class TokenEvent {

  private StateToken stateToken;
  private boolean canceled;

  public TokenEvent(StateToken stateToken) {
    this.stateToken = stateToken;
  }

  public StateToken getStateToken() {
    return stateToken;
  }

  public boolean isCanceled() {
    return canceled;
  }

  public void cancel() {
    this.canceled = true;
  }
}
