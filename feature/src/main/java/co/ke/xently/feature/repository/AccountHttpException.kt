package co.ke.xently.feature.repository


import co.ke.xently.source.remote.HttpException

class AccountHttpException(
    val email: List<String> = emptyList(),
) : HttpException() {
    data class Error(
        val email: String,
    )

    override fun hasFieldErrors() = listOf(
        email,
    ).any { it.isNotEmpty() }
}


val AccountHttpException?.error: AccountHttpException.Error
    get() {
        return AccountHttpException.Error(
            this?.email?.joinToString("\n") ?: "",
        )
    }