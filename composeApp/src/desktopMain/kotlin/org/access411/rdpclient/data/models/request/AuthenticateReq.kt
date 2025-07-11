package org.access411.rdpclient.data.models.request

import com.google.gson.annotations.SerializedName

data class AuthenticateReq(
    val username: String,
    val password: String,
)