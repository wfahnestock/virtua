package org.access411.rdpclient.domain.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.access411.rdpclient.data.api.AppApiClient
import org.access411.rdpclient.data.models.VirtualMachine
import org.access411.rdpclient.data.models.response.AuthenticateRes
import org.access411.rdpclient.shared.UIState
import java.util.prefs.Preferences
import kotlin.String
import kotlin.io.encoding.Base64

class MainScreenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<AuthenticateRes>>(UIState.Idle())
    val uiState: StateFlow<UIState<AuthenticateRes>> = _uiState.asStateFlow()

    val pref by lazy { Preferences.userNodeForPackage(this::class.java) }

    // <editor-fold desc="Folder names for filter">
    val folderNames = listOf(
        "group-v176",
        "group-v190",
        "group-v191",
        "group-v192",
        "group-v193",
        "group-v28480",
        "group-v28481",
        "group-v30480",
        "group-v33211",
        "group-v33212",
        "group-v33213",
        "group-v44685",
        "group-v189"
    )
    // </editor-fold>

    val serverListLoading = mutableStateOf(false)

    private val _servers = MutableStateFlow<List<VirtualMachine>>(emptyList())
    val servers: StateFlow<List<VirtualMachine>> = _servers.asStateFlow()

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

                    loadServers()
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

    private fun loadServers() {
        viewModelScope.launch {
            // Set the list to be in a loading state
            serverListLoading.value = true

            val filterMap = createFolderFilters(folderNames)
            val token = pref.get("token", "")
            val headers = mapOf("vmware-api-session-id" to token)

            try {
                val tempServers = mutableListOf<VirtualMachine>()
                val serverListResponse = AppApiClient.apiService.getServerList(headers, filterMap)

                if (serverListResponse.servers.isNotEmpty()) {
                    serverListResponse.servers.forEach { server ->
                        val machineDetailsResponse = AppApiClient.apiService.getMachineDetails(headers, server.id)

                        val vm = VirtualMachine(
                            id = server.id,
                            hostName = server.name,
                            ipAddress = machineDetailsResponse.value.ipAddress,
                            family = machineDetailsResponse.value.family,
                            powerState = server.powerState,
                            displayOrder = 0,
                            description = "",
                            url = "",
                        )

                        tempServers.add(vm)
                    }
                }

                _servers.value = tempServers
                serverListLoading.value = false
            } catch (ex: Exception) {
                _uiState.value = UIState.Error(
                    error = ex,
                    message = ex.message ?: "An error occurred while building server list.",
                    title = "Error building server list"
                )
            }
        }
    }

    fun saveDescription() {

    }

    fun saveUrl() {

    }

    private fun createFolderFilters(folderNames: List<String>): Map<String, String> {
        return folderNames.mapIndexed { index, name ->
            "filter.folders.${index + 1}" to name
        }.toMap()
    }
}