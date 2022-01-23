package co.ke.xently.accounts.ui.password_reset

import co.ke.xently.source.remote.HttpException

internal class PasswordResetHttpException(
    val oldPassword: List<String> = emptyList(),
    val newPassword: List<String> = emptyList(),
) : HttpException()