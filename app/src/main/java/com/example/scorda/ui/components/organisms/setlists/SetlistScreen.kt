package com.example.scorda.ui.components.organisms.setlists

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.scorda.data.database.entities.Setlist
import com.example.scorda.ui.navigation.SetlistDetailRoute
import com.example.scorda.ui.navigation.SetlistListRoute
import com.example.scorda.ui.viewmodel.LocalScoreViewModel
import com.example.scorda.ui.viewmodel.SetlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    initialSetlistId: Long? = null,
    viewModel: SetlistViewModel = viewModel(factory = SetlistViewModel.Factory),
) {
    val scoreViewModel = LocalScoreViewModel.current
    // 1. Local NavController for internal navigation (List -> Detail)
    val localNavController = rememberNavController()
    val navBackStackEntry by localNavController.currentBackStackEntryAsState()

    val isDetailScreen = navBackStackEntry?.destination?.hasRoute<SetlistDetailRoute>() ?: false
    val currentRoute = if (isDetailScreen) {
        navBackStackEntry?.toRoute<SetlistDetailRoute>()
    } else {
        null
    }

    val setlists by viewModel.setlists.collectAsStateWithLifecycle()
    var hasNavigatedInitial by remember { mutableStateOf(false) }

    LaunchedEffect(setlists, initialSetlistId) {
        if (!hasNavigatedInitial && initialSetlistId != null && setlists.isNotEmpty()) {
            setlists.find { it.id == initialSetlistId }?.let { setlist ->
                localNavController.navigate(
                    SetlistDetailRoute(
                        setlistId = setlist.id,
                        setlistName = setlist.name
                    )
                )
                hasNavigatedInitial = true
            }
        }
    }

    BackHandler {
        if (isDetailScreen) {
            localNavController.popBackStack()
        } else {
            onClose()
        }
    }

    var isAddingSetlist by remember { mutableStateOf(false) }
    var editingSetlist by remember { mutableStateOf<Setlist?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = currentRoute?.setlistName ?: "Setlists")
                },
                navigationIcon = {
                    if (isDetailScreen) {
                        IconButton(onClick = { localNavController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Setlists"
                            )
                        }
                    }
                },
                actions = {
                    if (!isDetailScreen) {
                        IconButton(onClick = { isAddingSetlist = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Setlist")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            NavHost(
                navController = localNavController,
                startDestination = SetlistListRoute,
                modifier = Modifier.fillMaxSize()
            ) {
                composable<SetlistListRoute> {
                    SetlistList(
                        setlists = setlists,
                        onSetlistClick = { setlist ->
                            localNavController.navigate(
                                SetlistDetailRoute(
                                    setlistId = setlist.id,
                                    setlistName = setlist.name
                                )
                            )
                        },
                        onMoreClick = { editingSetlist = it }
                    )
                }
                composable<SetlistDetailRoute> { backStackEntry ->
                    val route: SetlistDetailRoute = backStackEntry.toRoute()

                    androidx.compose.runtime.LaunchedEffect(route.setlistId) {
                        viewModel.selectSetlist(route.setlistId)
                    }

                    val setlistWithDetails by viewModel.selectedSetlist
                        .collectAsStateWithLifecycle()
                    val scoreUiState by scoreViewModel.scoreUiState
                        .collectAsStateWithLifecycle()
                    val currentScoreId = scoreUiState.selectedScore?.score?.id

                    setlistWithDetails?.let {
                        SetlistDetail(
                            setlistWithDetails = it,
                            currentScoreId = currentScoreId,
                            onScoreClick = { score ->
                                scoreViewModel.openScoreInCurrentTab(score.score.id, it.setlist.id)
                                onClose()
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogs...
    if (isAddingSetlist) {
        SetlistDialog(
            onDismissRequest = { isAddingSetlist = false },
            onConfirm = { name ->
                viewModel.addSetlist(name)
                isAddingSetlist = false
            }
        )
    }

    if (editingSetlist != null) {
        SetlistDialog(
            setlist = editingSetlist,
            onDismissRequest = { editingSetlist = null },
            onConfirm = { name ->
                viewModel.updateSetlist(editingSetlist!!.copy(name = name))
                editingSetlist = null
            }
        )
    }
}
