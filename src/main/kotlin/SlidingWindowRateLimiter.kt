import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class SlidingWindowRateLimiter(
    private val capacity: Int,
    private val windowSizeSeconds: Int,
    bucketSize: Int,
) : RateLimiter {
    private val bucket = ConcurrentHashMap<Long, AtomicInteger>()
    private val bucketDuration = (windowSizeSeconds * 1000L) / bucketSize
    private val lock = ReentrantLock()

    override fun <T> acquire(function: () -> T): T {
        val currentTime = System.currentTimeMillis()
        val currentBucket = currentTime / bucketDuration

        validateAndDoCleanBucket(currentTime = currentTime)

        bucket.computeIfAbsent(currentBucket) { AtomicInteger(0) }.incrementAndGet()
        val totalCount = bucket.values.sumOf { it.get() }

        return if (totalCount <= capacity) {
            function.invoke()
        } else {
            bucket[currentBucket]?.decrementAndGet()
            throw RateLimitException()
        }
    }

    private fun validateAndDoCleanBucket(currentTime: Long) {
        val expired = currentTime - windowSizeSeconds * 1000L
        lock.lock()
        try {
            bucket.keys.removeIf { it * bucketDuration < expired }
        } finally {
            lock.unlock()
        }
    }
}
