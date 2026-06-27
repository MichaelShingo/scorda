package com.example.scorda.ui.components.organisms.navbar.musictools

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MusicVideo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scorda.R
import com.example.scorda.ui.components.organisms.drone.Drone
import com.example.scorda.ui.components.organisms.metronome.Metronome
import com.example.scorda.ui.components.organisms.tuner.Tuner
import kotlinx.coroutines.launch

data class MusicToolItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun MusicTools(modifier: Modifier = Modifier) {
    val tabs = listOf(
        MusicToolItem(
            title = stringResource(R.string.tools_metronome),
            icon = Icons.Rounded.MusicNote,
        ),
        MusicToolItem(
            title = stringResource(R.string.tools_tuner),
            icon = Icons.Rounded.MusicVideo
        ),
        MusicToolItem(
            title = stringResource(R.string.tools_drone),
            icon = Icons.Rounded.LibraryMusic
        )
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.width(300.dp)) {
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            divider = {}
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title
                        )
                    },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) { pageIndex ->
            when (pageIndex) {
                0 -> Metronome()
                1 -> Tuner()
                2 -> Drone()
            }
        }
    }
}