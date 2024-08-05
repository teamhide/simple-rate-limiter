import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SlidingWindowRateLimiterTest : BehaviorSpec({
    Given("허용치 윈도우를 넘어섰을 때") {
        val rateLimiter = SlidingWindowRateLimiter(capacity = 2, bucketSize = 2, windowSizeSeconds = 2)

        When("새로운 요청이 들어오면") {
            Then("예외가 발생한다") {
                shouldThrow<RateLimitException> {
                    rateLimiter.acquire {
                        run()
                    }
                    rateLimiter.acquire {
                        run()
                    }
                    rateLimiter.acquire {
                        run()
                    }
                }
            }
        }
    }

    Given("잔여 윈도우가 남아있을 때") {
        val rateLimiter = SlidingWindowRateLimiter(capacity = 2, bucketSize = 2, windowSizeSeconds = 2)

        When("새로운 요청이 들어오면") {
            val sut1 = rateLimiter.acquire {
                run()
            }
            val sut2 = rateLimiter.acquire {
                run()
            }

            Then("정상 처리된다") {
                sut1 shouldBe "Success"
                sut2 shouldBe "Success"
            }
        }
    }
})
