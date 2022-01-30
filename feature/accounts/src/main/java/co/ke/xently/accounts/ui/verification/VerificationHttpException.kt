package co.ke.xently.accounts.ui.verification

import co.ke.xently.source.remote.HttpException

internal class VerificationHttpException(val code: List<String> = emptyList()) : HttpException()