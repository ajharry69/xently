package co.ke.xently.data

data class Address(
    val id: Long = -1L,
    val town: String = "",
    val location: List<Double> = emptyList(),
) {
    val latitude: Double
        get() = location.component1()
    val longitude: Double
        get() = location.component2()
}