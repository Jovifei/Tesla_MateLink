package com.teslamatelink.data.repository

/**
 * Sealed class wrapping API call outcomes.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}
