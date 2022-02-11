package co.ke.xently.feature.ui

import junit.framework.TestCase
import org.junit.Assert

class DialogKtTest : TestCase() {
    fun `test getting date from single digit hour`() {
        Assert.assertNotNull(dateFromHourAndMinute(1, 11))
    }

    fun `test getting date from single digit minute`() {
        Assert.assertNotNull(dateFromHourAndMinute(11, 1))
    }

    fun `test getting date from single digit hour and minute`() {
        Assert.assertNotNull(dateFromHourAndMinute(1, 1))
    }

    fun `test getting date from double digit hour and minute`() {
        Assert.assertNotNull(dateFromHourAndMinute(11, 11))
    }
}