package com.example.scorda.ui.components.organisms.scoreDetailDialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorda.R
import com.example.scorda.data.database.entities.Composer
import com.example.scorda.data.database.relations.ScoreWithDetails
import com.example.scorda.ui.components.atoms.composerDropdownMenu.SearchableDropdownMenu
import com.example.scorda.ui.viewmodel.ComposerViewModel
import com.example.scorda.ui.viewmodel.ScoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreDetailDialog(
    scoreWithDetails: ScoreWithDetails,
    onDismissRequest: () -> Unit
) {
    val score = scoreWithDetails.score
    val scoreViewModel: ScoreViewModel = viewModel(factory = ScoreViewModel.Factory)
    val updateScore = scoreViewModel::updateScore
    val clearComposer = scoreViewModel::clearComposer

    val composerViewModel: ComposerViewModel = viewModel(factory = ComposerViewModel.Factory)
    val composers by composerViewModel.composers.collectAsStateWithLifecycle()
    val searchQuery by composerViewModel.searchQuery.collectAsStateWithLifecycle()
    val onQueryChange = composerViewModel::onQueryChange
    val insertComposerFromSearch = composerViewModel::insertComposerFromSearch
    val getComposerFullName = composerViewModel::getCommaSeparatedFullName


    fun handleComposerSelected(composer: Composer) {
        scoreViewModel.connectComposer(score, composer)
        composerViewModel.onQueryChange(composerViewModel.getCommaSeparatedFullName(composer))
    }

    fun handleInsert() {
        composerViewModel.insertComposerFromSearch { newComposer ->
            handleComposerSelected(
                newComposer
            )
        }
    }


    LaunchedEffect(scoreWithDetails.score.id) {
        scoreWithDetails.composer?.let {
            onQueryChange(composerViewModel.getCommaSeparatedFullName(it))
        } ?: run {
            onQueryChange("")
        }
    }

    val valueToAdd = composerViewModel.getCommaSeparatedNameFromQuery(searchQuery)

    fun onClearComposer() {
        clearComposer(score)
    }

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier,
        content = {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.dialog_close),
                            )
                        }
                    }
                    Box(modifier = Modifier) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                label = { Text(stringResource(R.string.score_title)) },
                                modifier = Modifier.fillMaxWidth(),
                                value = score.title,
                                onValueChange = { },
                                singleLine = true,
                            )

                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        SearchableDropdownMenu(
                            label = stringResource(R.string.score_composer),
                            items = composers,
                            convertItemToText = getComposerFullName,
                            searchQuery = searchQuery,
                            onQueryChange = onQueryChange,
                            onSelect = ::handleComposerSelected,
                            onInsert = ::handleInsert,
                            valueToAdd = valueToAdd,
                            onClear = ::onClearComposer
                        )
                    }

                }
            }
        }
    )
}