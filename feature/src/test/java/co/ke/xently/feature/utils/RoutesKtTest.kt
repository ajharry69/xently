package co.ke.xently.feature.utils

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test

class RoutesKtTest : TestCase() {

    @Test
    fun testBuildRoute() {
        Assert.assertEquals("path/", "path/".buildRoute())
        Assert.assertEquals("path//", "path/{id}/".buildRoute())
        Assert.assertEquals("path/1/", "path/{id}/".buildRoute("id" to 1))
    }

    @Test
    fun testBuildRouteForQueriedPath() {
        Assert.assertEquals("path/", "path/?key={key}".buildRoute())
        Assert.assertEquals("path/?key=val", "path/?key={key}".buildRoute("key" to "val"))
        Assert.assertEquals(
            "path/?key=val",
            "path/?key={key}&k2={k2}".buildRoute("key" to "val")
        )
        Assert.assertEquals(
            "path/?key=val&k2=k2v",
            "path/?key={key}&k2={k2}".buildRoute("key" to "val", "k2" to "k2v")
        )
        Assert.assertEquals(
            "path/1/?key=val",
            "path/{id}/?key={key}".buildRoute("id" to 1, "key" to "val")
        )
    }
}