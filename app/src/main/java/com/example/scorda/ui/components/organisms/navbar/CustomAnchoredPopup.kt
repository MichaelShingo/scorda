package com.example.scorda.ui.components.organisms.navbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun CustomAnchoredPopup(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPopupVisible by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { isPopupVisible = true }) {
            Icon(imageVector = icon, contentDescription = contentDescription)

        }

        if (isPopupVisible) {
            Popup(
                alignment = Alignment.BottomCenter,
                offset = IntOffset(0, 500),
                onDismissRequest = { isPopupVisible = false },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                )
            ) {
                Surface(
                    modifier = Modifier
                        .heightIn(min = 120.dp, max = 240.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 6.dp,
                    shadowElevation = 12.dp,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    content()
                }
            }
        }
    }
}