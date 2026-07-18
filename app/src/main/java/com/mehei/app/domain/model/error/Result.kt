package com.mehei.app.domain.model.error

/**
 * A discriminated union that encapsulates a successful outcome with a value of type [T]
 * or a failure with an error of type [E].
 */
sealed class Result<out T, out E : AppError> {
    data class Success<out T>(val data: T) : Result<T, Nothing>()
    data class Error<out E : AppError>(val error: E) : Result<Nothing, E>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun errorOrNull(): E? = when (this) {
        is Success -> null
        is Error -> error
    }
}

/**
 * Applies [action] if the result is [Result.Success].
 */
inline fun <T, E : AppError> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Applies [action] if the result is [Result.Error].
 */
inline fun <T, E : AppError> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    if (this is Result.Error) action(error)
    return this
}

/**
 * Maps the data if it is a success, otherwise returns the error.
 */
inline fun <T, R, E : AppError> Result<T, E>.map(transform: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(error)
    }
}
