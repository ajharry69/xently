package co.ke.xently.data

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test

class ShopTest : TestCase() {

    @Test
    fun testToString() {
        Assert.assertEquals("", Shop().toString())
        Assert.assertEquals("Quickmart", Shop(name = "Quickmart").toString())
        Assert.assertEquals("P001122", Shop(taxPin = "P001122").toString())
        Assert.assertEquals(
            "Quickmart, P001122",
            Shop(name = "Quickmart", taxPin = "P001122").toString(),
        )
    }
}