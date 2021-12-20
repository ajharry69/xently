package co.ke.xently.feature.utils

import junit.framework.TestCase
import org.junit.Assert

class NumbersTest : TestCase() {

    fun `test descriptive for numbers less than 1000`() {
        Assert.assertEquals("888", 888.descriptive())
        Assert.assertEquals("998", 998.descriptive())
        Assert.assertEquals("1.0k", 999.descriptive())
        Assert.assertEquals("1.0k", 999.9.descriptive())
        Assert.assertEquals("1.0k", 999.91.descriptive())
    }

    fun `test descriptive for numbers in thousands`() {
        Assert.assertEquals("1.0k", 1_000.descriptive())
        Assert.assertEquals("1.1k", 1_100.descriptive())
        Assert.assertEquals("1.1k", 1_100.9.descriptive())
        Assert.assertEquals("1.9k", 1_900.0.descriptive())
        Assert.assertEquals("1.9k", 1_909.0.descriptive())
        Assert.assertEquals("2.0k", 1_990.0.descriptive())
        Assert.assertEquals("1.0M", 999_999.91.descriptive())
    }

    fun `test descriptive for numbers in millions`() {
        Assert.assertEquals("1.0M", 1_000_000.descriptive())
        Assert.assertEquals("1.0M", 1_000_000.9.descriptive())
        Assert.assertEquals("1.1M", 1_090_000.91.descriptive())
        Assert.assertEquals("1.0B", 999_999_999.91.descriptive())
    }
}