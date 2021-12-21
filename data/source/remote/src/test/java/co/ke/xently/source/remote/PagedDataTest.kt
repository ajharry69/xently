package co.ke.xently.source.remote

import junit.framework.TestCase
import org.junit.Assert


class PagedDataTest : TestCase() {
    fun `test to remote key for default paged data`() {
        Assert.assertThrows(AssertionError::class.java, PagedData<String>()::toRemoteKey)
    }

    fun `test to remote key for paged data with only next`() {
        val data = PagedData<String>(
            next = "https://example.com/api/data/?page=2",
        )
        val remoteKey = data.toRemoteKey()

        assertEquals(remoteKey.endpoint, "/api/data/")
        assertEquals(remoteKey.nextPage, 2)
        assertNull(remoteKey.prevPage)
        assertEquals(remoteKey.totalItems, 0)
    }

    fun `test to remote key for paged data with only previous`() {
        val data = PagedData<String>(
            previous = "https://example.com/api/data/?page=1",
        )
        val remoteKey = data.toRemoteKey()

        assertEquals(remoteKey.endpoint, "/api/data/")
        assertNull(remoteKey.nextPage)
        assertEquals(remoteKey.prevPage, 1)
        assertEquals(remoteKey.totalItems, 0)
    }

    fun `test to remote key for paged data with both next and previous`() {
        val data = PagedData<String>(
            next = "https://example.com/api/data/?page=3",
            previous = "https://example.com/api/data/?page=1",
        )
        val remoteKey = data.toRemoteKey()

        assertEquals(remoteKey.endpoint, "/api/data/")
        assertEquals(remoteKey.nextPage, 3)
        assertEquals(remoteKey.prevPage, 1)
        assertEquals(remoteKey.totalItems, 0)
    }

    fun `test to remote key for paged data with multiple both next and previous`() {
        val data = PagedData<String>(
            next = "https://example.com/api/data/?page=5&page=6&page=4",
            previous = "https://example.com/api/data/?page=3&page=1&page=&page",
        )
        val remoteKey = data.toRemoteKey()

        assertEquals(remoteKey.endpoint, "/api/data/")
        assertEquals(remoteKey.nextPage, 6)
        assertEquals(remoteKey.prevPage, 3)
        assertEquals(remoteKey.totalItems, 0)
    }
}