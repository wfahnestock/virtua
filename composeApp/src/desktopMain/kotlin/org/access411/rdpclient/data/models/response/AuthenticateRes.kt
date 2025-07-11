package org.access411.rdpclient.data.models.response

import com.google.gson.annotations.SerializedName

data class AuthenticateRes(
    @SerializedName("value")
    val token: String? = null,
)