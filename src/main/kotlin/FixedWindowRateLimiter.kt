import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class FixedWindowRateLimiter(
    private val capacity: Int,
    private val windowSizeSeconds: Int,
) : RateLimiter {
    private val requestCount = AtomicInteger(0)
    private val lock = ReentrantLock()

    @Volatile
    private var windowStart = System.currentTimeMillis()

    override fun <T> acquire(function: () -> T): T {
        validateAndDoResetWindow()
        return if (requestCount.get() < capacity) {
            requestCount.incrementAndGet()
            function.invoke()
        } else {
            throw RateLimitException()
        }
    }

    private fun validateAndDoResetWindow() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - windowStart >= windowSizeSeconds * 1000) {
            lock.lock()
            try {
                if (currentTime - windowStart >= windowSizeSeconds * 1000) {
                    requestCount.set(0)
                    windowStart = currentTime
                }
            } finally {
                lock.unlock()
            }
        }
    }
}
