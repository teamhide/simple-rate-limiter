import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class FixedWindowRateLimiterTest : BehaviorSpec({
    Given("최대 3개의 허용량과 1초의 윈도우 사이즈를 가졌을 때") {
        val rateLimiter = FixedWindowRateLimiter(capacity = 3, windowSizeSeconds = 1)

        When("고정 사이즈보다 많은 요청이 들어오면") {
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
                    rateLimiter.acquire {
                        run()
                    }
                }
            }
        }
    }

    Given("최대 2개의 허용량과 1초의 윈도우 사이즈를 가졌을 때") {
        val rateLimiter = FixedWindowRateLimiter(capacity = 2, windowSizeSeconds = 1)

        When("고정 사이즈보다 적은 요청이 들어오면") {
            val sut1 = rateLimiter.acquire {
                run()
            }
            val sut2 = rateLimiter.acquire {
                run()
            }

            Then("정상적으로 실행된다") {
                sut1 shouldBe "Success"
                sut2 shouldBe "Success"
            }
        }
    }
})
