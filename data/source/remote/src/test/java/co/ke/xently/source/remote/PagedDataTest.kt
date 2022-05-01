package co.ke.xently.source.remote

import junit.framework.TestCase
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
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

        assertThat(remoteKey.endpoint, equalTo("/api/data/"))
        assertThat(remoteKey.nextPage, equalTo(2))
        assertThat(remoteKey.prevPage, nullValue())
        assertThat(remoteKey.totalItems, equalTo(0))
    }

    fun `test to remote key for paged data with only previous`() {
        val data = PagedData<String>(
            previous = "https://example.com/api/data/?page=1",
        )
        val remoteKey = data.toRemoteKey()

        assertThat(remoteKey.endpoint, equalTo("/api/data/"))
        assertThat(remoteKey.nextPage, nullValue())
        assertThat(remoteKey.prevPage, equalTo(1))
        assertThat(remoteKey.totalItems, equalTo(0))
    }

    fun `test to remote key for paged data with both next and previous`() {
        val data = PagedData<String>(
            next = "https://example.com/api/data/?page=3",
            previous = "https://example.com/api/data/?page=1",
        )
        val remoteKey = data.toRemoteKey()

        assertThat(remoteKey.endpoint, equalTo("/api/data/"))
        assertThat(remoteKey.nextPage, equalTo(3))
        assertThat(remoteKey.prevPage, equalTo(1))
        assertThat(remoteKey.totalItems, equalTo(0))
    }

    fun `test to remote key for paged data with multiple both next and previous`() {
        val data = PagedData<String>(
            next = "https://example.com/api/data/?page=5&page=6&page=4",
            previous = "https://example.com/api/data/?page=3&page=1&page=&page",
        )
        val remoteKey = data.toRemoteKey()

        assertThat(remoteKey.endpoint, equalTo("/api/data/"))
        assertThat(remoteKey.nextPage, equalTo(6))
        assertThat(remoteKey.prevPage, equalTo(3))
        assertThat(remoteKey.totalItems, equalTo(0))
    }
}