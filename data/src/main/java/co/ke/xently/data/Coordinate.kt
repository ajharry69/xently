package co.ke.xently.data

data class Coordinate(val lat: Double, val lon: Double) {
    override fun toString(): String {
        return "${lat},${lon}"
    }
}