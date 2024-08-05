import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe


fun run(): String {
    return "Success"
}

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
