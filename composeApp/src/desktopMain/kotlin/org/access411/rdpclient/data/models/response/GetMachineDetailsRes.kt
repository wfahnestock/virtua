package org.access411.rdpclient.data.models.response

import com.google.gson.annotations.SerializedName

data class GetMachineDetailsRes(
    @SerializedName("value")
    val value: MachineDetails
)

data class MachineDetails(
    @SerializedName("full_name")
    val fullName: MachineFullName,
    @SerializedName("name")
    val name: String,
    @SerializedName("ip_address")
    val ipAddress: String,
    @SerializedName("family")
    val family: String,
    @SerializedName("host_name")
    val hostName: String
)

data class MachineFullName(
    @SerializedName("args")
    val args: List<Any>,
    @SerializedName("default_message")
    val defaultMessage: String,
    @SerializedName("id")
    val id: String
)