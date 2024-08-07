import java.util.concurrent.ConcurrentSkipListSet

class SlidingWindowLogRateLimiter(
    private val capacity: Int,
    private val windowSizeSeconds: Int,
) : RateLimiter {
    private val bucket = ConcurrentSkipListSet<Long>()
    private val toSecondUnit = 1_000_000_000L

    override fun <T> acquire(function: () -> T): T {
        val currentNanoTime = System.nanoTime()
        validateAndDoRemoveExpiredRequest(currentNanoTime = currentNanoTime)

        if (bucket.size < capacity) {
            bucket.add(currentNanoTime)
            return function.invoke()
        } else {
            throw RateLimitException()
        }
    }

    private fun validateAndDoRemoveExpiredRequest(currentNanoTime: Long) {
        bucket.removeIf { it <  currentNanoTime - windowSizeSeconds * toSecondUnit}
    }
}
