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

import java.util.Optional;

public interface DominoHistory {

  /**
   * Create a listener that will listen for any change in the url.
   *
   * @param listener {@link StateListener}
   * @return {@link DirectState}
   */
  DirectState listen(StateListener listener);

  /**
   * Manually removes a listener
   *
   * @param listener {@link StateListener}
   */
  void removeListener(StateListener listener);

  /**
   * Create a listener that will listen for any change that matches the criteria defined by the
   * token filter.
   *
   * @param tokenFilter {@link TokenFilter}
   * @param listener {@link StateListener}
   * @return {@link DirectState}
   */
  DirectState listen(TokenFilter tokenFilter, StateListener listener);

  /**
   * Create a listener that will listen to all changes to the url and will be removed after being
   * fired if {removeOnComplete} is true
   *
   * @param listener {@link StateListener}
   * @param removeOnComplete boolean
   * @return {@link DirectState}
   */
  DirectState listen(StateListener listener, boolean removeOnComplete);

  /**
   * Create a listener that will listen for any change that matches the criteria defined by the
   * token filter and will be removed after being fired if {removeOnComplete} is true.
   *
   * @param tokenFilter {@link TokenFilter}
   * @param listener {@link StateListener}
   * @param removeOnComplete boolean
   * @return {@link DirectState}
   */
  DirectState listen(TokenFilter tokenFilter, StateListener listener, boolean removeOnComplete);

  /**
   * A flag to enable/disable listening to popstate events in case we have multiple instances of the
   * history in the same application
   *
   * @return boolean if we want to enable listening
   */
  default boolean isInformOnPopState() {
    return true;
  }

  /** Go back one step simulating a back button */
  void back();

  /** Go forward one step simulating a forward button */
  void forward();

  /**
   * The count if the current history entries
   *
   * @return int
   */
  int getHistoryEntriesCount();

  /**
   * Updates the URL to the specified token without firing URL change listeners. This method wraps
   * the provided string token into a {@link StateToken} and delegates to the other overloaded
   * `pushState` method.
   *
   * @param token the string representing the new state token
   * @deprecated Use {@link #pushState(StateToken)} instead for better token management and
   *     flexibility.
   */
  @Deprecated
  default void pushState(String token) {
    pushState(StateToken.of(token));
  }

  /**
   * Change the url to the specified token without firing url change listeners, sets the title of
   * the new page and assign the data to the new state.
   *
   * @param stateToken {@link StateToken}
   */
  void pushState(StateToken stateToken);

  /**
   * Change the url to the specified token without firing url change listeners, sets the title of
   * the new page and assign the data to the new state. In case the new token has expression
   * parameters in the form <b>:paramName</b> they will be replaced using the <b>parameters</b>
   *
   * @param stateToken {@link StateToken}
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  void pushState(StateToken stateToken, TokenParameter... parameters);

  /**
   * Changes the URL to the specified token without firing URL change listeners. Sets the title of
   * the new page and assigns the data to the new state. In case the new token has expression
   * parameters in the form ":paramName", they will be replaced using the provided parameters.
   *
   * @param stateToken the string representing the new state token
   * @param parameters a list of {@link TokenParameter} to replace expression parameters in the URL
   *     token
   * @deprecated Use {@link #pushState(StateToken, TokenParameter...)} instead for better token
   *     management and flexibility.
   */
  @Deprecated
  default void pushState(String stateToken, TokenParameter... parameters) {
    pushState(StateToken.of(stateToken), parameters);
  };

  /**
   * Fires a state change by converting the provided string token into a {@link StateToken} and then
   * triggering the associated state change logic. This method is a shorthand for invoking the
   * {@link #fireState(StateToken)} method with a {@link StateToken} created from the given string.
   *
   * @param token the string token representing the desired state to be fired
   * @deprecated Use {@link #fireState(StateToken)} directly with a {@link StateToken}.
   */
  @Deprecated
  default void fireState(String token) {
    fireState(StateToken.of(token));
  }

  /**
   * Change the url to the specified token and fire change listeners, sets the title of the new page
   * and assign the data to the new state.
   *
   * @param stateToken {@link StateToken}
   */
  void fireState(StateToken stateToken);

  /**
   * Change the url to the specified token and fire url change listeners, sets the title of the new
   * page and assign the data to the new state. In case the new token has expression parameters in
   * the form <b>:paramName</b> they will be replaced using the <b>parameters</b>
   *
   * @param stateToken {@link StateToken}
   * @param parameters a list of {@link TokenParameter} to be used to replace expression params in
   *     the url token
   */
  void fireState(StateToken stateToken, TokenParameter... parameters);

  /**
   * Converts the provided string-based state token to a {@link StateToken} and triggers a state
   * change. Expression parameters in the token in the form ":paramName" are replaced using
   * the provided parameters. This method is a shorthand for firing a state using a string token.
   *
   * @param stateToken the string representation of the state to be fired
   * @param parameters a list of {@link TokenParameter} to replace expression parameters in the state token
   * @deprecated Use {@link #fireState(StateToken*/
  @Deprecated
  default void fireState(String stateToken, TokenParameter... parameters) {
    fireState(StateToken.of(stateToken), parameters);
  }

  /**
   * Replace the current url with the specified token without firing url change listeners, sets the
   * title of the new page and assign the data to the new state.
   *
   * @param stateToken {@link StateToken}
   */
  void replaceState(StateToken stateToken);

  /**
   * Parse the current url and return an immutable instance of {@link HistoryToken}
   *
   * @return {@link HistoryToken}
   */
  HistoryToken currentToken();

  /**
   * The root path assigned with the history instance
   *
   * @return String
   */
  String getRootPath();

  /**
   * Set the root path for this DominoHistory instance
   *
   * @param path String new root path.
   */
  void setRootPath(String path);

  void addInterceptor(HistoryInterceptor interceptor);

  void removeInterceptor(HistoryInterceptor interceptor);

  void reload();

  /** A functional interface to define a listener to be called when url state is changed. */
  interface StateListener {
    /**
     * Called when url state is changed and receives the new {@link State}
     *
     * @param state {@link State} the new history state
     */
    void onPopState(State state);
  }

  /** The url state */
  interface State {
    /** @return String, the token root path */
    String rootPath();

    /**
     * The parsed token of the url
     *
     * @return {@link HistoryToken}
     */
    HistoryToken token();

    /**
     * The data assigned to the url state
     *
     * @return String
     */
    Optional<String> data();

    /**
     * The page title
     *
     * @return String
     */
    String title();

    /**
     * A Normalized token that contains the parsed and matched path and fragment parameters from the
     * token expressions
     *
     * @return {@link NormalizedToken}
     */
    NormalizedToken normalizedToken();

    void setNormalizedToken(NormalizedToken normalizedToken);

    default boolean isDirect() {
      return false;
    }
  }

  @FunctionalInterface
  interface DirectUrlHandler {
    /**
     * A function to handle the direct url state when we reload the page.
     *
     * @param state {@link State} the direct history state
     */
    void handle(State state);
  }

  /**
   * An interface to allow executing some function when we reload the page while having a token in
   * the url
   */
  interface DirectState {
    /** Call for any token in the browser on reload */
    void onDirectUrl();

    /**
     * Call only when the token matches the {@link TokenFilter} criteria
     *
     * @param tokenFilter {@link TokenFilter} represent the matching criteria
     */
    void onDirectUrl(TokenFilter tokenFilter);
  }
}
