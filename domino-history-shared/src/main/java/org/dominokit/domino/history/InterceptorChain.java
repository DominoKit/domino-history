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

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.nonNull;

public class InterceptorChain implements IsInterceptorChain {
  private final Deque<HistoryInterceptor> interceptors = new LinkedList<>();
  private final Runnable onCompleted;
  private TokenEvent activeEvent;

  public InterceptorChain(List<HistoryInterceptor> interceptors, Runnable onCompleteHandler) {
    interceptors.forEach(this.interceptors::push);
    this.onCompleted = onCompleteHandler;
  }

  public void intercept(TokenEvent event) {
    this.activeEvent = event;

    if (nonNull(interceptors.peek())) {
      HistoryInterceptor popped = interceptors.pop();
      popped.onBeforeChangeState(event, this);
      if (event.isCanceled()) {
        onCompleted.run();
      }
    }else {
      onCompleted.run();
    }
  }

  @Override
  public void next() {
    if (!activeEvent.isCanceled()) {
      intercept(activeEvent);
    }
  }
}
