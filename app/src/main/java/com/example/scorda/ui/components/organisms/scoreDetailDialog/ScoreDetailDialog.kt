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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scorda.R
import com.example.scorda.data.database.relations.ScoreWithDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreDetailDialog(
    scoreWithDetails: ScoreWithDetails,
    onDismissRequest: () -> Unit
) {
    val score = scoreWithDetails.score

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

                }
            }
        }
    )
}