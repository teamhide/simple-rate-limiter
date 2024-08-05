import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class LeakyBucketRateLimiterTest : BehaviorSpec({
    Given("잔여 할당량이 없을 때") {
        val rateLimiter = LeakyBucketRateLimiter(capacity = 0, leakRatePerSecond = 1)

        When("새로운 요청이 들어오면") {
            Then("예외가 발생한다") {
                shouldThrow<RateLimitException> {
                    rateLimiter.acquire {
                        run()
                    }
                }
            }
        }
    }

    Given("잔여 할당량이 있을 때") {
        val rateLimiter = LeakyBucketRateLimiter(capacity = 10, leakRatePerSecond = 10)

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
