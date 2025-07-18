package org.access411.rdpclient.interop

data class NetworkCredential(
    val domain: String = "",
    val password: String,
    val securePassword: String? = null,
    val userName: String,
)
