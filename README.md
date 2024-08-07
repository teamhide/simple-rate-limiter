## Simple Rate Limiter

For practice, make your own rate limiter!

### [Token bucket](https://github.com/teamhide/simple-rate-limiter/blob/main/src/main/kotlin/TokenBucketRateLimiter.kt)
Pros
- 매우 간단하기 때문에 구현이 쉽다.
- 메모리를 적게 사용한다.
- 스파이크성으로 들어오는 트래픽을 대응할 수 있다. 버켓에 토큰이 남아있는 한 모든 요청을 수용할 수 있다.

Cons
- 분산 환경에서 동일 유저가 동시에 요청을 보내면 레이스 컨디션이 발생할 수 있다. (대안으로 Lua 스크립트를 활용)

### [Leaky bucket](https://github.com/teamhide/simple-rate-limiter/blob/main/src/main/kotlin/LeakyBucketRateLimiter.kt)
Pros
- 제한된 큐 크기를 고려하면 효율적인 메모리를 사용한다.
- 요청이 고정된 속도로 처리되기 때문에 안정적인 속도가 필요한 유즈케이스에 적합하다.

Cons
- 트래픽이 폭주하는 경우 큐에 이전 요청들이 채워지기 때문에 해당 요청들이 제시간에 처리되지 않으면 새로운 요청들이 버려진다.

### [Fixed window](https://github.com/teamhide/simple-rate-limiter/blob/main/src/main/kotlin/FixedWindowRateLimiter.kt)
Pros
- 이해하기 쉽고 메모리 효율적이다.
- 한도가 단위 시간 윈도우 끝에서만 초기화되는 유즈케이스에 가장 적합하다. 예를 들어 분당 10개의 제한이라면 매 윈도우(오전 10:00:00 ~ 10:00:59)에서 10개의 요청을 허용하여 오전 10:01:00에 한도가 초기화된다. 오전 10:00:30 ~ 오전 10:01:29 사이에 20개의 요청이 허용되었다 하더라도 오전 10:00:00 ~ 오전 10:00:59 까지가 하나의 윈도우이고 오전 10:01:00 ~ 오전 10:01:59까지는 또 다른 윈도우이기 때문에 문제가 없다. 

Cons
- 윈도우 끝 시점에 트래픽이 급증하는 경우 실시간으로 윈도우를 추적해야하는 유즈케이스에 적합하지 않다.
- 예제 1: 분당 10개의 제한일 때 유저가 0:59에 10개의 메시지를 받고 1:01에 10개의 메시지를 받을 수 있다.
- 예제 2: 분당 10개의 제한일 때 요청이 10:00:30에 시작하여 10:00:59에 끝났다면 추가 요청들은 10:01:29까지 허용되지 않는다. 10:00:30에 요청이 시작되었기 때문에 추가 요청에 대한 한도는 1분 이후인 10:01:29에 초기화된다.

### Sliding window logs
Pros
- 요청이 정확히 언제 발생했는지를 기록하고 그에 기반하여 판단하기 때문에 매우 정밀하게 요청을 제어할 수 있다.  

Cons
- 요청이 버려져도 타임스탬프를 Sorted Set에 저장하기 때문에 많은 메모리를 사용한다.

### [Sliding window counter](https://github.com/teamhide/simple-rate-limiter/blob/main/src/main/kotlin/SlidingWindowCounterRateLimiter.kt)
Pros
- 구현이 비교적 간단하며 매우 높은 정확도를 가진다.
- 각 서브 윈도우의 카운트만 저장하면 되기 때문에 메모리 사용량이 로그 방식보다 적다.

Cons
- 엄격하지 않은 윈도우에만 적용된다. 실제 요청 비율에 대한 근사치일 뿐이며 이는 이전 시간 윈도우의 요청이 균등하게 분포되어 있다고 가정하기 때문이다. 하지만 Cloudflare의 실험에 따르면 4억건의 요청 중 단 0.003%만이 잘못 허용/제한되었다고 하니 그리 큰 문제는 아닐 수 있다.

### Article

https://hides.kr/1152
