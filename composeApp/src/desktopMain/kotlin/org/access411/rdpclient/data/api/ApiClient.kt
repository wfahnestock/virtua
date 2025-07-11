package org.access411.rdpclient.data.api

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.CallConverterFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.hostIsIp
import io.ktor.serialization.gson.gson
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

object ApiClient {
    private const val BASE_URL = "https://10.1.100.14/"

    val client = HttpClient(CIO) {
        engine {
            https {
                // Disable SSL chain checks - we have a self-signed certificate
                trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) { }
                    override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) { }
                    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                }
            }
        }
        install(ContentNegotiation) {
            gson()
        }
    }

    val ktor = Ktorfit.Builder()
        .baseUrl(BASE_URL)
        .httpClient(client)
        .build()
}

object AppApiClient {
    val apiService = ApiClient.ktor.createApiServices()
}