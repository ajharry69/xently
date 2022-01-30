package co.ke.xently.accounts.ui.password_reset.request

import co.ke.xently.source.remote.HttpException

internal class PasswordResetRequestHttpException(
    val email: List<String> = emptyList(),
) : HttpException()