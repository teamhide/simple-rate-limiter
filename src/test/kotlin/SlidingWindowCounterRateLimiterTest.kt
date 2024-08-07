import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SlidingWindowCounterRateLimiterTest : BehaviorSpec({
    Given("허용치 윈도우를 넘어섰을 때") {
        val rateLimiter = SlidingWindowCounterRateLimiter(capacity = 2, bucketSize = 2, windowSizeSeconds = 2)

        When("새로운 요청이 들어오면") {
            Then("예외가 발생한다") {
                shouldThrow<RateLimitException> {
                    repeat(3) {
                        rateLimiter.acquire {
                            run()
                        }
                    }
                }
            }
        }
    }

    Given("잔여 윈도우가 남아있을 때") {
        val rateLimiter = SlidingWindowCounterRateLimiter(capacity = 2, bucketSize = 2, windowSizeSeconds = 2)
        val repeatCount = 2
        val result = mutableListOf<String>()

        When("새로운 요청이 들어오면") {
            repeat(repeatCount) {
                val sut = rateLimiter.acquire {
                    run()
                }
                result.add(sut)
            }

            Then("정상 처리된다") {
                result.size shouldBe repeatCount
            }
        }
    }
})
