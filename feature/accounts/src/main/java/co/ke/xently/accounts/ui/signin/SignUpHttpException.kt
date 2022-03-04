package co.ke.xently.accounts.ui.signin

import co.ke.xently.source.remote.HttpException

internal class SignUpHttpException(
    val email: List<String> = emptyList(),
    val password: List<String> = emptyList(),
) : HttpException() {
    override fun hasFieldErrors(): Boolean {
        return arrayOf(email, password).any { it.isNotEmpty() }
    }
}