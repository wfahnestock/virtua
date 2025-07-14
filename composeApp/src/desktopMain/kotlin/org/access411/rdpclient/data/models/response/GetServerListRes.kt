package org.access411.rdpclient.data.models.response

import com.google.gson.annotations.SerializedName

data class GetServerListRes(
    @SerializedName("value")
    val servers: List<Server>
)

data class Server(
    @SerializedName("memory_size_MiB")
    val memorySizeMb: Int,
    @SerializedName("vm")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("power_state")
    val powerState: String,
    @SerializedName("cpu_count")
    val cpuCount: Int
)