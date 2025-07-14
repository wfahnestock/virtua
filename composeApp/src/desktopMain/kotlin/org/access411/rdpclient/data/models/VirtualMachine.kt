package org.access411.rdpclient.data.models

data class VirtualMachine(
    val id: String,
    val hostName: String,
    val ipAddress: String,
    val family: String,
    val powerState: String,
    val displayOrder: Int,
    val description: String,
    val url: String,
)