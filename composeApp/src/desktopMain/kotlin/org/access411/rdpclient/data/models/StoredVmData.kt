package org.access411.rdpclient.data.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class StoredVmData(
    @SerializedName("Id")
    val Id: String,
    @SerializedName("Description")
    val Description: String = "",
    @SerializedName("Url")
    val Url: String = "",
    @SerializedName("DisplayOrder")
    val DisplayOrder: Int = 99999
)

fun VirtualMachine.toStoredData(): StoredVmData {
    return StoredVmData(
        Id = this.id,
        Description = this.description,
        Url = this.url,
        DisplayOrder = this.displayOrder
    )
}

fun StoredVmData.toVirtualMachine(): VirtualMachine {
    return VirtualMachine(
        id = this.Id,
        hostName = "",
        description = this.Description,
        url = this.Url,
        ipAddress = "",
        family = "",
        powerState = "",
        displayOrder = this.DisplayOrder
    )
}