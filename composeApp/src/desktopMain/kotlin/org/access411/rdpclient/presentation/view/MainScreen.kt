package org.access411.rdpclient.presentation.view

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import org.access411.rdpclient.cursorForHorizontalResize
import org.access411.rdpclient.domain.viewmodels.MainScreenViewModel
import org.access411.rdpclient.shared.UIState
import org.access411.rdpclient.shared.preference
import org.access411.rdpclient.ui.theme.LocalAppColors
import org.access411.rdpclient.ui.theme.Typography

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel { MainScreenViewModel() }
) {
    val serverListState = rememberLazyListState()
    var selectedIndex by remember { mutableStateOf( -1) }
    var splitRatio by remember { mutableStateOf(0.5f) }

    val uiState by viewModel.uiState.collectAsState()

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
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFf4f4f4)),
                            state = serverListState
                        ) {
                            items(50) { index ->

                                ServerListItem(
                                    name = "Server $index",
                                    modifier = Modifier
                                        .selectable(
                                            selected = (selectedIndex == index),
                                            onClick = {
                                                selectedIndex = if (selectedIndex != index) index
                                                else -1
                                            }
                                        )
                                        .background(if (selectedIndex == index) Color(0xFF177bbd) else Color.Transparent)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(
                                scrollState = serverListState
                            )
                        )
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
                    text = "An error occurred while contacting the server.\nVerify connection and try again.\n\nError: ${(uiState as UIState.Error).message}",
                    fontWeight = FontWeight.Bold,
                    fontSize = Typography.titleLarge.fontSize,
                    modifier = Modifier.padding(top = 8.dp)
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