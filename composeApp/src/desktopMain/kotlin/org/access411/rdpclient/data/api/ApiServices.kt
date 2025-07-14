package org.access411.rdpclient.data.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.HeaderMap
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.QueryMap
import org.access411.rdpclient.data.models.response.AuthenticateRes
import org.access411.rdpclient.data.models.response.GetMachineDetailsRes
import org.access411.rdpclient.data.models.response.GetServerListRes

interface ApiServices {

    @POST("rest/com/vmware/cis/session")
    suspend fun authenticate(@Header("Authorization") authHeader: String): AuthenticateRes

    @GET("rest/vcenter/vm")
    suspend fun getServerList(
        @HeaderMap headers: Map<String, String>,
        @QueryMap folderFilters: Map<String, String> = emptyMap()
    ): GetServerListRes

    @GET("rest/vcenter/vm/{vm}/guest/identity")
    suspend fun getMachineDetails(
        @HeaderMap headers: Map<String, String>,
        @Path("vm") vm: String
    ): GetMachineDetailsRes
}