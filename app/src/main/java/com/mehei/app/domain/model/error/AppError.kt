package com.mehei.app.domain.model.error

/**
 * Base sealed class for all application domain errors.
 * Enforces structured, typed error handling instead of raw exceptions.
 */
sealed class AppError(open val message: String) {
    data class NetworkError(override val message: String = "Network connection failed") : AppError(message)
    data class DatabaseError(override val message: String = "Database operation failed") : AppError(message)
    data class NotFoundError(val resource: String, val id: String) : AppError("$resource with ID $id not found")
    data class ValidationError(override val message: String) : AppError(message)
    data class UnauthorizedError(override val message: String = "Authentication required") : AppError(message)
    data class UnknownError(val throwable: Throwable? = null) : AppError(throwable?.message ?: "An unknown error occurred")
}
