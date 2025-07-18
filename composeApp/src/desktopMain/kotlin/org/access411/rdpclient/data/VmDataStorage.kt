package org.access411.rdpclient.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.access411.rdpclient.data.models.StoredVmData
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.collections.emptyMap

class VmDataStorage {
    private var file: File
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; coerceInputValues = true}

    init {
        // Get AppData path and create directories if needed
        val appDataPath = Paths.get(
            System.getProperty("user.home"),
            "AppData", "Local", "Access411 RDP Client"
        )
        Files.createDirectories(appDataPath)
        file = appDataPath.resolve("vm_data.json").toFile()
    }

    fun loadVmData(): Map<String, StoredVmData> {
        if (!file.exists()) {
            return emptyMap()
        }

        return try {
            val content = file.readText()
            if (content.isBlank()) {
                emptyMap()
            } else {
                json.decodeFromString(content)
            }
        } catch (e: Exception) {
            println("Error loading VM data: ${e.message}")
            emptyMap()
        }
    }

    fun saveVmData(vmData: Map<String, StoredVmData>) {
        try {
            val jsonContent = json.encodeToString(vmData)
            file.writeText(jsonContent)
        } catch (e: Exception) {
            println("Error saving VM data: ${e.message}")
        }
    }
}