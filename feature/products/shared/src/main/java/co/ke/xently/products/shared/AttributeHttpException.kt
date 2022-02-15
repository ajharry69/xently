package co.ke.xently.products.shared

import co.ke.xently.source.remote.HttpException

data class AttributeHttpException(
    val name: List<String> = emptyList(),
    val value: List<String> = emptyList(),
) : HttpException() {
    override fun hasFieldErrors() = listOf(name, value).any { it.isNotEmpty() }
}