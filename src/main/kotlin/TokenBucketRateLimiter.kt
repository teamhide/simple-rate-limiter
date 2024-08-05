import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class TokenBucketRateLimiter(
    private val capacity: Int,
    private val refillRateSeconds: Int,
) : RateLimiter {
    private val bucket = AtomicInteger(capacity)
    private val lock = ReentrantLock()

    @Volatile
    private var lastRefillTime = System.currentTimeMillis()

    override fun <T> acquire(function: () -> T): T {
        validateAndDoRefill()
        return if (bucket.get() > 0) {
            bucket.decrementAndGet()
            function.invoke()
        } else {
            throw RateLimitException()
        }
    }

    private fun validateAndDoRefill() {
        val currentTime = System.currentTimeMillis()
        val timeElapsed = currentTime - lastRefillTime
        val refillTokens = (timeElapsed / 1000) * refillRateSeconds

        if (refillTokens > 0) {
            lock.lock()
            try {
                val newTokens = refillTokens.toInt().coerceAtMost(capacity - bucket.get())
                if (newTokens > 0) {
                    bucket.addAndGet(newTokens)
                    lastRefillTime = currentTime
                }
            } finally {
                lock.unlock()
            }
        }
    }
}
