package co.ke.xently.common

import junit.framework.TestCase
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest as runCoroutineTest

class RetryTest : TestCase() {

    fun `test can retry when number of retries is greater than 1`() = runCoroutineTest {
        val retry = Retry(number = 2)
        assertTrue(retry.canRetry())
        // Default number...
        assertTrue(Retry().canRetry())
    }

    fun `test can retry when number of retries is 1`() = runCoroutineTest {
        val retry = Retry(number = 1)
        assertTrue(retry.canRetry())
    }

    fun `test can not retry when number of retries is 0`() = runCoroutineTest {
        val retry = Retry(number = 0)
        assertFalse(retry.canRetry())
    }

    fun `test can not retry when number of retries is -1`() = runCoroutineTest {
        val retry = Retry(number = -1)
        assertFalse(retry.canRetry())
    }

    fun `test can retry called number of retry times`() = runCoroutineTest {
        val retry = Retry()
        assertTrue(retry.canRetry())
        assertTrue(retry.canRetry())
        assertTrue(retry.canRetry())
    }

    fun `test can retry called more than number of retry times`() = runCoroutineTest {
        val retry = Retry()

        assertEquals(retry.currentAttemptCount, 1)
        assertEquals(retry.currentWaitTimeSeconds.seconds, 3.seconds)
        assertTrue(retry.canRetry())

        assertEquals(retry.currentAttemptCount, 2)
        assertEquals(retry.currentWaitTimeSeconds.seconds, 6.seconds)
        assertTrue(retry.canRetry())

        assertEquals(retry.currentAttemptCount, 3)
        assertEquals(retry.currentWaitTimeSeconds.seconds, 12.seconds)
        assertTrue(retry.canRetry())

        // Configured number of retries exceeded
        assertEquals(retry.currentAttemptCount, 4)
        assertEquals(retry.currentWaitTimeSeconds.seconds, 24.seconds)
        assertFalse(retry.canRetry())

        // Neither wait time nor attempt count is not increased once number of retries is reached
        assertEquals(retry.currentAttemptCount, 4)
        assertEquals(retry.currentWaitTimeSeconds.seconds, 24.seconds)
    }

    fun `test can retry called more than number of retry times with non-default backoff multiplier`() =
        runCoroutineTest {
            val retry = Retry(backoffMultiplier = 2)

            assertEquals(retry.currentAttemptCount, 1)
            assertEquals(retry.currentWaitTimeSeconds.seconds, 3.seconds)
            assertTrue(retry.canRetry())

            assertEquals(retry.currentAttemptCount, 2)
            assertEquals(retry.currentWaitTimeSeconds.seconds, 9.seconds)
            assertTrue(retry.canRetry())

            assertEquals(retry.currentAttemptCount, 3)
            assertEquals(retry.currentWaitTimeSeconds.seconds, 27.seconds)
            assertTrue(retry.canRetry())

            // Configured number of retries exceeded
            assertEquals(retry.currentAttemptCount, 4)
            assertEquals(retry.currentWaitTimeSeconds.seconds, 81.seconds)
            assertFalse(retry.canRetry())

            // Neither wait time nor attempt count is not increased once number of retries is reached
            assertEquals(retry.currentAttemptCount, 4)
            assertEquals(retry.currentWaitTimeSeconds.seconds, 81.seconds)
        }

    fun `test can retry is independent of retry class instance`() = runCoroutineTest {
        Retry(number = 2).apply {
            assertTrue(isDefaultState)

            assertTrue(canRetry())
            assertFalse(isDefaultState)

            assertTrue(canRetry())
            assertFalse(canRetry())

            assertFalse(isDefaultState)
        }.apply {
            assertFalse(canRetry())
            assertFalse(isDefaultState)
        }.copy(number = 1).apply {
            assertTrue(isDefaultState)
            assertTrue(canRetry())

            assertFalse(isDefaultState)
            assertFalse(canRetry())
        }.copy(number = 3).apply {
            assertTrue(isDefaultState)
            assertTrue(canRetry())

            assertFalse(isDefaultState)
            assertTrue(canRetry())
            assertTrue(canRetry())
            assertFalse(canRetry())
        }
    }
}