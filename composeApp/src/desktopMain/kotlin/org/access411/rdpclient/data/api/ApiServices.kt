package org.access411.rdpclient.data.api

import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import org.access411.rdpclient.data.models.response.AuthenticateRes

interface ApiServices {

    @POST("rest/com/vmware/cis/session")
    suspend fun authenticate(@Header("Authorization") authHeader: String): AuthenticateRes
}