package co.ke.xently.products.shared

import co.ke.xently.source.remote.HttpException

data class BrandHttpException(val name: List<String> = emptyList()) : HttpException() {
    override fun hasFieldErrors() = listOf(name).any { it.isNotEmpty() }
}