package com.example.scorda.ui.components.organisms.navbar.musictools

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scorda.R
import com.example.scorda.ui.components.organisms.drone.Drone
import com.example.scorda.ui.components.organisms.metronome.Metronome
import com.example.scorda.ui.components.organisms.tuner.Tuner
import kotlinx.coroutines.launch

@Composable
fun MusicTools(modifier: Modifier = Modifier) {
    val tabs = listOf(
        stringResource(R.string.tools_metronome),
        stringResource(R.string.tools_tuner),
        stringResource(R.string.tools_drone)
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.width(300.dp)) {
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            when (pageIndex) {
                0 -> Metronome()
                1 -> Tuner()
                2 -> Drone()
            }
        }
    }

}