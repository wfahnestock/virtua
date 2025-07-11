package org.access411.rdpclient.shared

sealed class UIState<out T> {
    class Idle(): UIState<Nothing>()
    class Loading(val progress: Int = 0): UIState<Nothing>()
    class Success<out T>(val data: T?): UIState<T>()
    class Error(
        val error: Throwable? = null,
        val message: String? = null,
        val title: String? = null
    ): UIState<Nothing>()
}