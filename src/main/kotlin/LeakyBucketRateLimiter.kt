import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class LeakyBucketRateLimiter(
    private val capacity: Int,
    private val leakRatePerSecond: Int,
) : RateLimiter {
    private val bucket = AtomicInteger(0)
    private val lock = ReentrantLock()

    @Volatile
    private var lastLeakTime = System.currentTimeMillis()

    override fun <T> acquire(function: () -> T): T {
        validateAndDoLeak()
        return if (bucket.get() < capacity) {
            bucket.incrementAndGet()
            function.invoke()
        } else {
            throw RateLimitException()
        }
    }

    private fun validateAndDoLeak() {
        val currentTime = System.currentTimeMillis()
        val timeElapsed = (currentTime - lastLeakTime) / 1000
        val tokensToLeak = (timeElapsed * leakRatePerSecond).toInt()

        if (tokensToLeak > 0) {
            lock.lock()
            try {
                val tokensToActuallyLeak = tokensToLeak.coerceAtMost(bucket.get())
                bucket.addAndGet(-tokensToActuallyLeak)
                lastLeakTime = currentTime
            } finally {
                lock.unlock()
            }
        }
    }
}
