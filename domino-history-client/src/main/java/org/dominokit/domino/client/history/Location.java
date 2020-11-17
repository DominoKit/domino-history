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

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Location {

  public native void assign(String newLocation);

  public native void replace(String newLocation);

  public native void reload();

  public native String toString();

  @JsProperty
  public native String getHref();

  @JsProperty
  public native String getProtocol();

  @JsProperty
  public native String getHost();

  @JsProperty
  public native String getHostname();

  @JsProperty
  public native String getPort();

  @JsProperty
  public native String getPathname();

  @JsProperty
  public native String getSearch();

  @JsProperty
  public native String getHash();

  @JsProperty
  public native String getUsername();

  @JsProperty
  public native String getPassword();

  @JsProperty
  public native String getOrigin();
}
