package co.ke.xently.data

import junit.framework.TestCase
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class ShopTest : TestCase() {

    @Test
    fun testToString() {
        assertThat(Shop().toString(), emptyString())
        assertThat(Shop(name = "Quickmart").toString(), equalTo("Quickmart"))
        assertThat(Shop(taxPin = "P001122").toString(), equalTo("P001122"))
        assertThat(
            Shop(name = "Quickmart", taxPin = "P001122").toString(),
            equalTo("Quickmart, P001122"),
        )
    }
}