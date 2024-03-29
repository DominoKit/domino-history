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
package org.dominokit.domino.client.history;

import elemental2.dom.DomGlobal;
import java.util.Optional;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.dominokit.domino.history.EffectiveToken;

/** A JsInterop implementation to represent the current state of the page on the browser. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class JsState {
  /** The url in the browser */
  public String historyToken;
  /** Data String assigned to the current browser state, on refresh or reload this is always null */
  public String data;
  /** The title of the current page */
  public String title;

  @JsOverlay
  public static JsState state(EffectiveToken token) {
    JsState state = new JsState();
    state.historyToken = token.getToken();
    state.data = Optional.ofNullable(token.getData()).orElse("");
    state.title = Optional.ofNullable(token.getTitle()).orElse(DomGlobal.document.title);

    return state;
  }
}
