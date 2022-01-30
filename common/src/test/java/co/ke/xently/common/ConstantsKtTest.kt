package co.ke.xently.common

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import org.junit.function.ThrowingRunnable

class ConstantsKtTest : TestCase() {

    @Test
    fun testReplaceAt() {
        assertEquals("H3llo", "Hello".replaceAt(1, "3"))
        assertEquals("H33llo", "Hello".replaceAt(1, "33"))
    }

    @Test
    fun testReplaceAtForIndexOutOfBounds() {
        Assert.assertThrows(IndexOutOfBoundsException::class.java, object : ThrowingRunnable {
            override fun run() {
                "Hello".replaceAt(-1, "33")
            }
        })
        Assert.assertThrows(IndexOutOfBoundsException::class.java, object : ThrowingRunnable {
            override fun run() {
                "Hello".replaceAt(5, "33")
            }
        })
    }
}