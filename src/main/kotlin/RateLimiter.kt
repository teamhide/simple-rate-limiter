interface RateLimiter {
    fun <T> acquire(function: () -> T): T
}
