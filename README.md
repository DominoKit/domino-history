![logoimage](https://raw.githubusercontent.com/DominoKit/DominoKit.github.io/master/logo/128.png)

<a title="Gitter" href="https://gitter.im/DominoKit/domino"><img src="https://badges.gitter.im/Join%20Chat.svg"></a>
[![Development Build Status](https://github.com/DominoKit/domino-history/actions/workflows/deploy.yaml/badge.svg?branch=development)](https://github.com/DominoKit/domino-history/actions/workflows/deploy.yaml/badge.svg?branch=development)
![Maven Central](https://img.shields.io/badge/Release-1.0.0--RC3-green)
![Sonatype Nexus (Snapshots)](https://img.shields.io/badge/Snapshot-HEAD--SNAPSHOT-orange)
![GWT3/J2CL compatible](https://img.shields.io/badge/GWT3/J2CL-compatible-brightgreen.svg)

# domino-history
A wrapper for browser history state API

### Maven dependencies 

- **Release**

```xml
<dependency>
  <groupId>org.dominokit</groupId>
  <artifactId>domino-history-client</artifactId>
  <version>1.0.0-RC3</version>
</dependency>
```

- **Development snapshot**

```xml
<dependency>
  <groupId>org.dominokit</groupId>
  <artifactId>domino-history-client</artifactId>
  <version>HEAD-SNAPSHOT</version>
</dependency>
```
### GWT inherits

`<inherits name="org.dominokit.domino.client.history.History"/>`

### Usage

1- Create a `StateHistory` instance.

`StateHistory history = new StateHistory()`

2- Use `pushState(String token)` to change the window url without firing the events.

3- Use `fireState(String token)` to change the window url and fire the events.

4- Use `listen` with all its variants to start listing for url changes.

5- Use `currentToken` to get the current active token in the browser window url.

#### Sample

```java
public class App implements EntryPoint {

    private StateHistory history = new StateHistory();

    public void onModuleLoad() {

        history.listen(TokenFilter.any(), state -> {
            console.info(state.token().value());
        });
        history.listen(TokenFilter.startsWithPathFilter("path1"), state -> {
            console.info(state.token().value());
        });

        HTMLButtonElement button1 = Js.uncheckedCast(document.createElement("button"));
        button1.textContent ="button1";
        button1.addEventListener("click", evt -> history.pushState("path1/path2"));

        HTMLButtonElement button2 = Js.uncheckedCast(document.createElement("button"));
        button2.textContent ="button2";
        button2.addEventListener("click", evt -> history.fireState("path3/path4"));

        HTMLButtonElement button3 = Js.uncheckedCast(document.createElement("button"));
        button3.textContent ="button3";
        button3.addEventListener("click", evt -> console.info("current token : "+history.currentToken().value()));

        document.body.appendChild(button1);
        document.body.appendChild(button2);
        document.body.appendChild(button3);

    }
}
```

### The HistoryToken

to get the current url token use `history().currentToken()`, every time this method is called it will return a new `HistoryToken` instance. once a token is obtained we can use it to manipulate the token instance, use one of the following methods to make changes to the token or ask for information from the token : 

* **`boolean startsWithPath(String path)`** : returns true if the token starts with the path.
* **`boolean endsWithPath(String path)`** : returns true if the paths part ends with the path.
* **`boolean containsPath(String path)`** : returns true if the paths contains the path.
* **`List<String> paths()`** : returns a list of all paths in the token.
* **`String path()`** : returns the paths part of the token as a String.
* **`HistoryToken appendPath(String path)`** : add a new path at the end the token path.
* **`HistoryToken replacePath(String path, String replacement)`** : replace a specific path with a new one.
* **`HistoryToken replaceAllPaths(String newPath)`** : replace all paths with a new path.
* **`HistoryToken removePath(String path)`** : removes a specific path from the token.
* **`HistoryToken removeLastPath(String path)`** : removes the last path part from the token.
* **`HistoryToken replaceLastPath(String replacement)`** : replace the last path in token with a new path.
* **`HistoryToken clearPaths()`** : remove all paths from the token.
* **`boolean fragmentsStartsWith(String fragment)`** : return true if the part after the `#` starts with the fragment.
* **`boolean endsWithFragment(String fragment)`** : return true if the part after the `#` ends with the fragment.
* **`boolean containsFragment(String fragment)`** : return true if the part after the `#` contains the fragment.
* **`List<String> fragments()`** : return a list of fragments.
* **`HistoryToken replaceLastFragment(String replacement)`** : replace the last fragment with a new one.
* **`HistoryToken removeFragment(String fragment)`** : remove a specific fragment.
* **`HistoryToken appendFragment(String fragment)`** : append a fragment at the end of the fragments
* **`HistoryToken clearFragments()`** : clear all fragments.
* **`HistoryToken replaceFragment(String fragment, String replacement)`** : replace a fragment with a new one.
* **`HistoryToken replaceAllFragments(String newFragment)`** : replace all fragments with a new one.
* **`String fragment()`** : return the fragments as a String.
* **`Map<String, String> queryParameters()`** : returns a map of all query parameters.
* **`boolean hasQueryParameter(String name)`** : returns true if there is a query name with the specified name.
* **`String getQueryParameter(String name)`** : return the value of the specified query parametr.
* **`HistoryToken appendParameter(String name, String value)`** : add a new query parameter.
* **`HistoryToken replaceParameter(String name, String replacementName, String replacementValue)`** : replace a query parameter with a new one.
* **`HistoryToken removeParameter(String name)`** : removes a query parameter.
* **`HistoryToken replaceQuery(String newQuery)`** : replace all query part with a new one.
* **`HistoryToken clearQuery()`** : remove all query parameters.
* **`String query()`** : return the query parameters as a String.
* **`boolean isEmpty()`** : returns true if the token is empty.
* **`HistoryToken clear()`** : remove all paths, query parameters and fragments and makes the token empty.
* **`String value()`** : return the token as a String.

### Token Filters

Use one of the filters from `TokenFilter` to narrow the tokens you want to listen to from history, the TokenFilter class has factory method for lots of useful filters.

- Sample

```java
history.listen(TokenFilter.any(), state -> {
    console.info(state.token().value());
});
history.listen(TokenFilter.startsWithPathFilter("path1"), state -> {
    console.info(state.token().value());
});
```
#### Built-in token filters

* **`exactMatch(String matchingToken)`**

* **`startsWith(String prefix)`**

* **`endsWith(String postfix)`**

* **`contains(String part)`**

* **`any()`**

* **`exactFragmentMatch(String matchingToken)`**

* **`startsWithFragment(String prefix)`**

* **`endsWithFragment(String postfix)`**

* **`containsFragment(String part)`**

* **`anyFragment()`**

* **`hasPathFilter(String path)`**

* **`hasPathsFilter(String... paths)`**

* **`exactPathFilter(String path)`**

* **`startsWithPathFilter(String path)`**

* **`endsWithPathFilter(String path)`**

* **`anyPathFilter()`**

* **`isEmpty()`**

### Wildcards

Sometimes we want to listen to token where a specific part of it is a variable, in this case we can simply define that part as a variable using `:` in the token filter when we define a listener.

- Sample

```java
HTMLButtonElement button4 = Js.uncheckedCast(document.createElement("button"));
button4.textContent ="button4";
button4.addEventListener("click", evt -> history.fireState("fixedPath/somePath"));

history.listen(TokenFilter.endsWith("fixedPath/:variablePath"), state -> {
    String variablePathValue = state.normalizedToken().getPathParameter("variablePath");
    console.info(variablePathValue);
    //when the button clicked should print somePath
});
``` 



