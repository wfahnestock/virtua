package org.access411.rdpclient.domain.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import de.jensklingenberg.ktorfit.Callback
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.access411.rdpclient.App
import org.access411.rdpclient.data.api.AppApiClient
import org.access411.rdpclient.data.models.request.AuthenticateReq
import org.access411.rdpclient.data.models.response.AuthenticateRes
import org.access411.rdpclient.main
import org.access411.rdpclient.shared.UIState
import org.access411.rdpclient.shared.preference
import java.util.prefs.Preferences
import kotlin.io.encoding.Base64

class MainScreenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<AuthenticateRes>>(UIState.Idle())
    val uiState: StateFlow<UIState<AuthenticateRes>> = _uiState.asStateFlow()

    val pref by lazy { Preferences.userNodeForPackage(this::class.java) }

    init {
        viewModelScope.launch {
            _uiState.value = UIState.Loading()

            val loginStr = "wfahnestock@vsphere.local:g388p%gTN0&AOeT2r"
            val loginBase64 = Base64.Default.encode(loginStr.encodeToByteArray())
            val authHeader = "Basic $loginBase64"

            try {
                val response = AppApiClient.apiService.authenticate(authHeader)

                if (response.token != null) {
                    pref.put("token", response.token)
                    _uiState.value = UIState.Success(response)
                }
            } catch (ex: Exception) {
                _uiState.value = UIState.Error(
                    error = ex,
                    message = ex.message ?: "An error occurred while authenticating.",
                    title = "Authentication Error"
                )
            }
        }
    }
}