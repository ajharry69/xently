package co.ke.xently.feature.utils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class NumbersKtTest {

    @Test
    fun floatForDisplay() {
        assertThat(1f.forDisplay, equalTo("1"))
        assertThat(1.1f.forDisplay, equalTo("1.1"))
        assertThat(1.01f.forDisplay, equalTo("1.01"))
        assertThat(1.010f.forDisplay, equalTo("1.01"))
    }

    @Test
    fun negativeFloatForDisplay() {
        assertThat((-1f).forDisplay, equalTo("-1"))
        assertThat((-1.1f).forDisplay, equalTo("-1.1"))
        assertThat((-1.01f).forDisplay, equalTo("-1.01"))
        assertThat((-1.010f).forDisplay, equalTo("-1.01"))
    }

    @Test
    fun doubleForDisplay() {
        assertThat(1.0.forDisplay, equalTo("1"))
        assertThat(1.1.forDisplay, equalTo("1.1"))
        assertThat(1.01.forDisplay, equalTo("1.01"))
        assertThat(1.010.forDisplay, equalTo("1.01"))
    }

    @Test
    fun negativeDoubleForDisplay() {
        assertThat((-1.0).forDisplay, equalTo("-1"))
        assertThat((-1.1).forDisplay, equalTo("-1.1"))
        assertThat((-1.01).forDisplay, equalTo("-1.01"))
        assertThat((-1.010).forDisplay, equalTo("-1.01"))
    }

    @Test
    fun integerForDisplay() {
        assertThat(1.forDisplay, equalTo("1"))
        assertThat(1_000.forDisplay, equalTo("1000"))
    }

    @Test
    fun negativeIntegerForDisplay() {
        assertThat((-1).forDisplay, equalTo("-1"))
        assertThat((-1_000).forDisplay, equalTo("-1000"))
    }
}