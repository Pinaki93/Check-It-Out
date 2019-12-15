package dev.pinaki.todoapp.ds

sealed class Result<out T : Any> {
    object Loading : Result<Nothing>()

    data class Success<out T : Any>(val data: T? = null) : Result<T>()

    data class Error(val cause: Throwable) : Result<Nothing>()
}