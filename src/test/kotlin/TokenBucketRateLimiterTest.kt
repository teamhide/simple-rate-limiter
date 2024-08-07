import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class TokenBucketRateLimiterTest : BehaviorSpec({
    Given("버킷에 토큰이 모두 소진되었을 때") {
        val rateLimiter = TokenBucketRateLimiter(capacity = 0, refillRateSeconds = 10)

        When("새로운 요청이 들어오면") {
            Then("예외가 발생한다") {
                shouldThrow<RateLimitException> {
                    rateLimiter.acquire { run() }
                }
            }
        }
    }

    Given("버킷에 잔여 토큰이 존재할 때") {
        val rateLimiter = TokenBucketRateLimiter(capacity = 2, refillRateSeconds = 10)
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
