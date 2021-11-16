package co.ke.xently.data

sealed class TaskResult<out R> {
    data class Success<out T>(val data: T) : TaskResult<T>() {
        override fun toString(): String = super.toString()
    }

    data class Error(val error: Throwable) : TaskResult<Nothing>() {
        constructor(error: String) : this(Exception(error))
    }

    companion object Loading : TaskResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success -> super.toString()
            is Error -> error.message!!
            is Loading -> "Loading..."
        }
    }
}

fun <T> TaskResult<T>.getOrNull(): T? = if (this is TaskResult.Success) data else null

fun <T> TaskResult<T>.getOrThrow(): T = try {
    getOrNull()!!
} catch (ex: Exception) {
    throw Exception("Invalid ${TaskResult::class.java.simpleName} type! ${ex.message}")
}

inline fun <T, R> T.runCatching(block: T.() -> R): TaskResult<R> {
    return try {
        TaskResult.Success(block())
    } catch (e: Throwable) {
        TaskResult.Error(e)
    }
}

inline fun <R, T> TaskResult<T>.mapCatching(transform: (value: T) -> R): TaskResult<R> {
    return when (this) {
        is TaskResult.Success -> runCatching {
            transform(data)
        }
        is TaskResult.Error -> this
        else -> TaskResult.Loading
    }
}

inline val TaskResult<*>.errorMessage
    get() = if (this is TaskResult.Error) error.localizedMessage ?: error.message else null
