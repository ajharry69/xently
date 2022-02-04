package co.ke.xently.data

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test

class ShopTest : TestCase() {

    @Test
    fun testToString() {
        Assert.assertEquals(Shop().toString(), "")
        Assert.assertEquals(Shop(name = "Quickmart").toString(), "Quickmart")
        Assert.assertEquals(Shop(taxPin = "P001122").toString(), "P001122")
        Assert.assertEquals(Shop(name = "Quickmart", taxPin = "P001122").toString(),
            "Quickmart, P001122")
    }
}