package co.ke.xently.common.utils

enum class AuthScheme(private val value: String) {
    BEARER("Bearer"),
    BASIC("Basic"),;

    override fun toString(): String = value
}