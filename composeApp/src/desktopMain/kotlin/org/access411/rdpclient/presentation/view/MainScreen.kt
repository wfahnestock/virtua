package org.access411.rdpclient.presentation.view

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.DataTableState
import com.seanproctor.datatable.TableColumnWidth
import com.seanproctor.datatable.material3.DataTable
import kotlinx.coroutines.launch
import org.access411.rdpclient.cursorForHorizontalResize
import org.access411.rdpclient.data.models.VirtualMachine
import org.access411.rdpclient.domain.viewmodels.MainScreenViewModel
import org.access411.rdpclient.shared.UIState
import org.access411.rdpclient.ui.theme.LocalAppColors
import org.access411.rdpclient.ui.theme.Typography
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel { MainScreenViewModel() }
) {
    var selectedIndex by remember { mutableStateOf( -1) }
    var splitRatio by remember { mutableStateOf(0.5f) }

    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val selectedServer = remember { mutableStateOf<VirtualMachine?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().safeContentPadding().padding(4.dp)
    ) {
        if (uiState is UIState.Loading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = Color(0xFFfdb724),
                    trackColor = Color.DarkGray
                )

                Text(
                    text = "Loading, please wait...",
                    fontWeight = FontWeight.Bold,
                    fontSize = Typography.titleLarge.fontSize,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        if (uiState is UIState.Success) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                /**
                 * Server List container
                 */
                Column(
                    modifier = Modifier
                        .weight(splitRatio)
                        .padding(end = 4.dp)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Server List",
                        fontWeight = FontWeight.Bold,
                        fontSize = Typography.titleLarge.fontSize
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val servers by viewModel.servers.collectAsState()

                        if (viewModel.serverListLoading.value) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(Alignment.Center),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = Color(0xFFfdb724),
                                    trackColor = Color.DarkGray
                                )

                                Text(
                                    text = "Loading server list...",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                )
                            }
                        } else {
                            val scrollState = remember(selectedIndex) { DataTableState() }
                            var sortColumnIndex by remember { mutableStateOf<Int?>(null) }
                            var sortAscending by remember { mutableStateOf<Boolean>(false) }

                            val sortedServers = remember(servers, sortColumnIndex, sortAscending) {
                                when (sortColumnIndex) {
                                    0 -> {
                                        if (sortAscending) {
                                            servers.sortedBy { it.hostName }
                                        } else {
                                            servers.sortedByDescending { it.hostName }
                                        }
                                    }
                                    1 -> {
                                        if (sortAscending) {
                                            servers.sortedBy { it.description }
                                        } else {
                                            servers.sortedByDescending { it.description }
                                        }
                                    }
                                    2 -> {
                                        if (sortAscending) {
                                            servers.sortedBy { it.ipAddress }
                                        } else {
                                            servers.sortedByDescending { it.ipAddress }
                                        }
                                    }
                                    else -> servers
                                }
                            }

                            // Drag & Drop Impl
                            val draggedIndex = remember { mutableStateOf<Int?>(null) }
                            val orderableServers = remember(sortedServers) {
                                mutableStateListOf<VirtualMachine>().also { it.addAll(sortedServers) }
                            }
                            val density = LocalDensity.current

                            DataTable(
                                columns = listOf(
                                    DataColumn(width = TableColumnWidth.Wrap) { },
                                    DataColumn(
                                        onSort = { index, sorted ->
                                            sortColumnIndex = index
                                            sortAscending = sorted
                                        }
                                    ) {
                                        Text(
                                            text = "Name",
                                            color = Color(0xff177bbd)
                                        )
                                    },
                                    DataColumn(
                                        onSort = { index, sorted ->
                                            sortColumnIndex = index
                                            sortAscending = sorted
                                        }
                                    ) {
                                        Text(
                                            text = "Description",
                                            color = Color(0xff177bbd)
                                        )
                                    },
                                    DataColumn(
                                        alignment = Alignment.CenterEnd,
                                        onSort = { index, sorted ->
                                            sortColumnIndex = index
                                            sortAscending = sorted
                                        }
                                    ) {
                                        Text(
                                            text = "IP Address",
                                            color = Color(0xff177bbd)
                                        )
                                    },
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                rowHeight = 36.dp,
                                sortColumnIndex = sortColumnIndex,
                                state = scrollState,
                                separator = {  },
                                contentPadding = PaddingValues(8.dp),
                                sortAscending = sortAscending
                            ) {
                                for ((index, server) in orderableServers.withIndex()) {
                                    row {
                                        onClick = { selectedServer.value = server }
                                        backgroundColor = if (server == selectedServer.value)
                                            Color(0xFF177bbd)
                                        else if (draggedIndex.value == index)
                                            Color(0x33177bbd)
                                        else
                                            Color.Transparent

                                        // Drag handle cell
                                        cell {
                                            Icon(
                                                imageVector = Icons.Filled.Reorder,
                                                contentDescription = "Drag to reorder",
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .pointerInput(index) {
                                                        detectDragGestures(
                                                            onDragStart = {
                                                                draggedIndex.value = index
                                                            },
                                                            onDragEnd = {
                                                                draggedIndex.value = null
                                                            },
                                                            onDragCancel = {
                                                                draggedIndex.value = null
                                                            },
                                                            onDrag = { change, _ ->
                                                                change.consume()

                                                                // Get current drag position and scroll offset
                                                                val dragY = change.position.y
                                                                val scrollOffset = scrollState.verticalScrollState.offset

                                                                // Calculate absolute position accounting for scroll
                                                                val absoluteY = dragY + scrollOffset

                                                                // Convert to row index with local density
                                                                val rowHeightPx = with(density) { 36.dp.toPx() }
                                                                val targetRow = (absoluteY / rowHeightPx).toInt()
                                                                    .coerceIn(0, orderableServers.lastIndex)

                                                                // Only move if we have a valid drag index and different target
                                                                draggedIndex.value?.let { currentIndex ->
                                                                    if (targetRow != currentIndex) {
                                                                        val item = orderableServers.removeAt(currentIndex)
                                                                        orderableServers.add(targetRow, item)
                                                                        draggedIndex.value = targetRow

                                                                        // Ensure the dragged item is visible by auto-scrolling
                                                                        scope.launch {
                                                                            scrollState.verticalScrollState.scrollBy(
                                                                                when {
                                                                                    // When near top, scroll down to reveal more items above
                                                                                    dragY < 100 -> 20f
                                                                                    // When near bottom, scroll up to reveal more items below
                                                                                    dragY > scrollState.verticalScrollState.viewportSize - 100 -> -20f
                                                                                    else -> 0f
                                                                                }
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        )
                                                    }
                                            )
                                        }

                                        cell { Text(text = server.hostName) }
                                        cell { Text(text = server.description) }
                                        cell { Text(text = server.ipAddress) }
                                    }
                                }
                            }

                            // Create a custom adapter for the vertical scrollbar using DataTableState
                            val verticalScrollAdapter = remember(scrollState) {
                                object : ScrollbarAdapter {
                                    override val scrollOffset: Double
                                        get() = scrollState.verticalScrollState.offset.toDouble()
                                    override val contentSize: Double
                                        get() = scrollState.verticalScrollState.totalSize.toDouble()
                                    override val viewportSize: Double
                                        get() = scrollState.verticalScrollState.viewportSize.toDouble()
                                    override suspend fun scrollTo(scrollOffset: Double) {
                                        scrollState.verticalScrollState.scrollTo(scrollOffset.roundToInt())
                                    }
                                }
                            }

                            VerticalScrollbar(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight()
                                    .padding(bottom = 12.dp), // Leave space for horizontal scrollbar
                                adapter = verticalScrollAdapter
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = "Drag to resize",
                    modifier = Modifier
                        .width(12.dp)
                        .height(48.dp)
                        .cursorForHorizontalResize()
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                val newRatio = splitRatio + delta / 1000
                                splitRatio = newRatio.coerceIn(0.2f, 0.8f)
                            }
                        )
                        .align(Alignment.CenterVertically)
                )

                /**
                 * Details Container
                 */
                Column(
                    modifier = Modifier
                        .weight(1f - splitRatio)
                        .padding(start = 4.dp)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Server Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = Typography.titleLarge.fontSize
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp)
                    ) {
                        if (selectedServer.value == null) {
                            // Show message when no server is selected
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Select a server to view details",
                                    color = Color.Gray,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        } else {
                            // Server details view
                            val server = selectedServer.value!!

                            // State for editable fields only
                            val editableDescription = remember(server) { mutableStateOf(server.description) }
                            val editableUrl = remember(server) { mutableStateOf(server.url) }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                // Display read-only server information
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Host Name:", fontWeight = FontWeight.Bold)
                                        Text(server.hostName, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp))

                                        Text("IP Address:", fontWeight = FontWeight.Bold)
                                        Text(server.ipAddress, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Family:", fontWeight = FontWeight.Bold)
                                        Text(server.family, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp))

                                        Text("Power State:", fontWeight = FontWeight.Bold)
                                        Text(server.powerState, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Editable fields section
                                Text(
                                    text = "Description",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                OutlinedTextField(
                                    value = editableDescription.value,
                                    onValueChange = { editableDescription.value = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Gray,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Color(0xff177bbd),
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "CAASS URL",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                OutlinedTextField(
                                    value = editableUrl.value,
                                    onValueChange = { editableUrl.value = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    supportingText = {
                                        Text(
                                            text = "ex: https://testing.access411.com/",
                                            color = Color.Gray,
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Gray,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Color(0xff177bbd),
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Action buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            // Connect to server
                                            //viewModel.connectToServer(server)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Connect")
                                    }

                                    Button(
                                        onClick = {
                                            // Save changes to description and URL
//                                            viewModel.updateServer(
//                                                server.copy(
//                                                    description = editableDescription.value,
//                                                    url = editableUrl.value
//                                                )
//                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Save")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState is UIState.Error) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalAppColors.current.error),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Dangerous,
                    contentDescription = "error",
                    modifier = Modifier
                        .size(64.dp)
                )

                Text(
                    text = "${(uiState as UIState.Error).title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = Typography.titleLarge.fontSize,
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                )

                Text(
                    text = "An error occurred while contacting the server.\nVerify connection and try again.\n\nError: ${(uiState as UIState.Error).message}",
                    fontWeight = FontWeight.Bold,
                    fontSize = Typography.titleLarge.fontSize,
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ServerListItem(
    name: String,
    modifier: Modifier
) {


    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier,
            text = name,
            fontSize = Typography.bodyLarge.fontSize,
            fontStyle = FontStyle.Italic
        )
    }
}