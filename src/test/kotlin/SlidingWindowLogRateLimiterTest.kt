import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SlidingWindowLogRateLimiterTest : BehaviorSpec({
    Given("현재 윈도우가 꽉 찰때까지") {
        val rateLimiter = SlidingWindowLogRateLimiter(capacity = 10, windowSizeSeconds = 1)

        When("새로운 요청이 오면") {
            Then("예외가 발생한다") {
                shouldThrow<RateLimitException> {
                    repeat(100) {
                        rateLimiter.acquire {
                            run()
                        }
                    }
                }
            }
        }
    }

    Given("현재 윈도우가 꽉 차지 않을 때 까지") {
        val rateLimiter = SlidingWindowLogRateLimiter(capacity = 10, windowSizeSeconds = 1)
        val repeatCount = 10
        val result = mutableListOf<String>()

        When("새로운 요청이 오면") {
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
