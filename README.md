![logoimage](https://raw.githubusercontent.com/DominoKit/DominoKit.github.io/master/logo/128.png)

<a title="Gitter" href="https://gitter.im/DominoKit/domino"><img src="https://badges.gitter.im/Join%20Chat.svg"></a>
[![Development Build Status](https://github.com/DominoKit/domino-history/actions/workflows/deploy.yaml/badge.svg?branch=development)](https://github.com/DominoKit/domino-history/actions/workflows/deploy.yaml/badge.svg?branch=development)
![Maven Central](https://img.shields.io/badge/Release-1.0.0--RC3-green)
![Sonatype Nexus (Snapshots)](https://img.shields.io/badge/Snapshot-HEAD--SNAPSHOT-orange)
![GWT3/J2CL compatible](https://img.shields.io/badge/GWT3/J2CL-compatible-brightgreen.svg)

# domino-history
A History API wrapper for browser (GWT) apps with a JVM in-memory implementation for
examples, tests, or desktop applications. It provides a consistent token model, filtering,
normalization (wildcards), and an interceptor chain for navigation guards.

## What it provides
- Browser implementation built on the History API: `StateHistory`
- JVM implementation for tests or non-browser apps: `JVMHistory`
- Shared token model: paths, query parameters, fragments
- Token filters with wildcards and normalization
- Interceptors to block or modify navigation
- Direct URL handling on reload (useful for deep links)

## Modules
- `domino-history-shared`: shared token model and filters
- `domino-history-client`: browser GWT implementation
- `domino-history-jvm`: JVM in-memory implementation
- `domino-history-test`: test helpers and tests

## Installation
### Maven dependencies
Use the latest release from Maven Central (example shown).

- Browser / GWT
```xml
<dependency>
  <groupId>org.dominokit</groupId>
  <artifactId>domino-history-client</artifactId>
  <version>1.0.0-RC3</version>
</dependency>
```

- JVM / tests
```xml
<dependency>
  <groupId>org.dominokit</groupId>
  <artifactId>domino-history-jvm</artifactId>
  <version>1.0.0-RC3</version>
</dependency>
```

- Development snapshot
```xml
<dependency>
  <groupId>org.dominokit</groupId>
  <artifactId>domino-history-client</artifactId>
  <version>HEAD-SNAPSHOT</version>
</dependency>
```

### GWT inherits
Use the client module for browser applications:

`<inherits name="org.dominokit.domino.client.history.History"/>`

If you only need the shared token utilities:

`<inherits name="org.dominokit.domino.history.History"/>`

## Quick start (browser)
```java
public class App implements EntryPoint {

  private final StateHistory history = new StateHistory();

  @Override
  public void onModuleLoad() {
    history.listen(TokenFilter.startsWith("users/:userId"), state -> {
      String userId = state.normalizedToken().getPathParameter("userId");
      console.info("User: " + userId);
    });

    history.fireState(
        StateToken.of("users/:userId")
            .title("User profile")
            .data("{\"from\":\"profile\"}"),
        TokenParameter.of("userId", "42"),
        TokenParameter.query("tab", "activity"));
  }
}
```

## Quick start (JVM)
```java
JVMHistory history = new JVMHistory();
history.listen(TokenFilter.any(), state -> {
  System.out.println(state.token().value());
});

history.fireState(StateToken.of("dashboard"));
history.back();
```

## Core concepts
### StateToken
Use `StateToken` to include a token string, optional title, and optional data payload.
`StateHistory` applies it to the browser URL; `JVMHistory` stores it in memory.

### HistoryToken
`HistoryToken` is a parsed view of the URL token with helpers to:
- Read/modify paths, query parameters, and fragments
- Check prefixes/suffixes/contains
- Serialize back to string via `value()` or `noRootValue()`

### TokenFilter
Use filters to target specific tokens. Examples:
- `TokenFilter.startsWith("orders/:orderId")`
- `TokenFilter.exactMatch("dashboard")`
- `TokenFilter.and(TokenFilter.startsWith("users"), TokenFilter.queryParam("active", "true"))`

### NormalizedToken (wildcards)
When a filter contains variables (ex: `:userId`), the matched values are available through
`state.normalizedToken().getPathParameter("userId")`.

## Token format
Tokens are treated as:
- Paths: `a/b/c`
- Query: `?sort=asc&tag=domino`
- Fragments: `#details/section`

Example token: `users/42?tab=profile#details/intro`

## Navigation APIs
- `pushState(StateToken)` updates the URL without notifying listeners.
- `fireState(StateToken)` updates the URL and fires matching listeners.
- `replaceState(StateToken)` replaces the current entry.
- `currentToken()` parses the current URL into a `HistoryToken`.
- `back()` / `forward()` walk the history stack.

## Direct URL handling
When the application loads with a URL (deep-link), invoke your listener directly:

```java
history.listen(TokenFilter.any(), state -> {
  console.info("Direct? " + state.isDirect());
}).onDirectUrl();
```

## Interceptors (navigation guards)
Interceptors can cancel or allow navigation before it is applied.

```java
history.addInterceptor(
    (event, chain) -> {
      if (!allowNavigation(event.getParsedToken())) {
        event.cancel();
        return;
      }
      chain.next();
    });
```

## Root paths
You can scope a history instance to a root path:

```java
StateHistory history = new StateHistory("app");
// Tokens must start with "app" to match listeners.
```

## Token utilities and mutation
`HistoryToken` supports:
- Path operations: append/replace/remove segments
- Query operations: add/replace/remove parameters (multi-value supported)
- Fragment operations: add/replace/remove fragment segments

## Token filters (built-in)
`TokenFilter` includes:
- `exactMatch`, `startsWith`, `endsWith`, `contains`, `any`
- Fragment variants: `exactFragmentMatch`, `startsWithFragment`, `endsWithFragment`, `containsFragment`, `anyFragment`
- Path variants: `hasPathFilter`, `hasPathsFilter`, `exactPathFilter`, `startsWithPathFilter`, `endsWithPathFilter`, `anyPathFilter`
- `queryParam`, `isEmpty`, `not`, `and`, `or`

## JVM history notes
`JVMHistory` keeps a virtual history stack (`back`/`forward`) and is ideal for tests.

## Build and test
- `mvn test`
- `mvn -pl domino-history-shared -am test` (targeted module build)
