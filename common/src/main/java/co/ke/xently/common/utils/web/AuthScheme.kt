package co.ke.xently.common.utils.web

enum class AuthScheme(val value: String) {
    BEARER("Bearer"),
    BASIC("Basic"),;

    override fun toString(): String = value
}