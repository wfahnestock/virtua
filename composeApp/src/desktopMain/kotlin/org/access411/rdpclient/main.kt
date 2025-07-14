package org.access411.rdpclient

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {

    val windowState = WindowState(
        width = 1280.dp,
        height = 720.dp,
        placement = WindowPlacement.Floating,
        position = WindowPosition.Aligned(
            alignment = Alignment.Center
        )
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "Access411 RDP Client",
        state = windowState
    ) {
        App()
    }
}