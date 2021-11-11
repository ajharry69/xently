package co.ke.xently.common

import junit.framework.TestCase
import kotlinx.coroutines.test.runBlockingTest
import kotlin.time.Duration

class RetryTest : TestCase() {

    fun `test can retry when number of retries is greater than 1`() = runBlockingTest {
        val retry = Retry(number = 2)
        assertTrue(retry.canRetry())
        // Default number...
        assertTrue(Retry().canRetry())
    }

    fun `test can retry when number of retries is 1`() = runBlockingTest {
        val retry = Retry(number = 1)
        assertTrue(retry.canRetry())
    }

    fun `test can not retry when number of retries is 0`() = runBlockingTest {
        val retry = Retry(number = 0)
        assertFalse(retry.canRetry())
    }

    fun `test can not retry when number of retries is -1`() = runBlockingTest {
        val retry = Retry(number = -1)
        assertFalse(retry.canRetry())
    }

    fun `test can retry called number of retry times`() = runBlockingTest {
        val retry = Retry()
        assertTrue(retry.canRetry())
        assertTrue(retry.canRetry())
        assertTrue(retry.canRetry())
    }

    fun `test can retry called more than number of retry times`() = runBlockingTest {
        val retry = Retry()

        assertEquals(retry.currentAttemptCount, 1)
        assertEquals(Duration.seconds(retry.currentWaitTimeSeconds), Duration.seconds(3))
        assertTrue(retry.canRetry())

        assertEquals(retry.currentAttemptCount, 2)
        assertEquals(Duration.seconds(retry.currentWaitTimeSeconds), Duration.seconds(6))
        assertTrue(retry.canRetry())

        assertEquals(retry.currentAttemptCount, 3)
        assertEquals(Duration.seconds(retry.currentWaitTimeSeconds), Duration.seconds(12))
        assertTrue(retry.canRetry())

        // Configured number of retries exceeded
        assertEquals(retry.currentAttemptCount, 4)
        assertEquals(Duration.seconds(retry.currentWaitTimeSeconds), Duration.seconds(24))
        assertFalse(retry.canRetry())

        // Neither wait time nor attempt count is not increased once number of retries is reached
        assertEquals(retry.currentAttemptCount, 4)
        assertEquals(Duration.seconds(retry.currentWaitTimeSeconds), Duration.seconds(24))
    }

    fun `test can retry called more than number of retry times with non-default backoff multiplier`() =
        runBlockingTest {
            val retry = Retry(backoffMultiplier = 2)

            assertEquals(retry.currentAttemptCount, 1)
            assertEquals(Duration.seconds(retry.currentWaitTimeSeconds), Duration.seconds(3))
            assertTrue(retry.canRetry())

            assertEquals(retry.currentAttemptCount, 2)
            assertEquals(Duration.seconds(retry.currentWaitTimeSeconds), Duration.seconds(9))
            assertTrue(retry.canRetry())

            assertEquals(retry.currentAttemptCount, 3)
            assertEquals(Duration.seconds(retry.currentWaitTimeSeconds), Duration.seconds(27))
            assertTrue(retry.canRetry())

            // Configured number of retries exceeded
            assertEquals(retry.currentAttemptCount, 4)
            assertEquals(Duration.seconds(retry.currentWaitTimeSeconds), Duration.seconds(81))
            assertFalse(retry.canRetry())

            // Neither wait time nor attempt count is not increased once number of retries is reached
            assertEquals(retry.currentAttemptCount, 4)
            assertEquals(Duration.seconds(retry.currentWaitTimeSeconds), Duration.seconds(81))
        }

    fun `test can retry is independent of retry class instance`() = runBlockingTest {
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