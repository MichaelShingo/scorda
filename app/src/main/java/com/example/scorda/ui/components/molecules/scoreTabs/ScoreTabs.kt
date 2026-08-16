package com.example.scorda.ui.components.molecules.scoreTabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.scorda.ui.components.organisms.navbar.AnchoredPopup
import com.example.scorda.ui.components.organisms.navbar.CustomAnchoredPopupSize
import com.example.scorda.ui.components.organisms.searchScores.SearchScores
import com.example.scorda.ui.viewmodel.OpenScoreTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreTabs(
    openTabs: List<OpenScoreTab>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onTabClosed: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeTabIndex =
        if (openTabs.isEmpty()) 0 else selectedTabIndex.coerceIn(0, openTabs.size - 1)
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SecondaryScrollableTabRow(
                selectedTabIndex = safeTabIndex,
                modifier = Modifier.weight(1f),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {},
            ) {
                openTabs.forEachIndexed { index, tab ->
                    CompositionLocalProvider(
                        LocalMinimumInteractiveComponentSize provides 0.dp
                    ) {
                        Tab(
                            selected = safeTabIndex == index,
                            onClick = { onTabSelected(index) },
                            text = {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                        .widthIn(min = 48.dp, max = 150.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = tab.scoreDetails.score.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    IconButton(
                                        onClick = { onTabClosed(index) },
                                        modifier = Modifier
                                            .padding(start = 2.dp)
                                            .size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close Tab",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }

            AnchoredPopup(
                size = CustomAnchoredPopupSize.Large,
                anchor = { onOpen, isExpanded ->
                    IconButton(
                        onClick = onOpen,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add New Tab",
                            tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            ) { onDismiss ->
                SearchScores(
                    onScoreClick = { onDismiss() }
                )
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}
