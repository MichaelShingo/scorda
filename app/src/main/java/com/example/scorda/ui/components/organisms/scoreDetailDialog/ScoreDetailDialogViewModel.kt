package com.example.scorda.ui.components.organisms.scoreDetailDialog

import androidx.lifecycle.ViewModel
import com.example.scorda.data.repository.ComposerRepository
import com.example.scorda.data.repository.ScoreRepository

class ScoreDetailDialogViewModel(
    private val scoreRepository: ScoreRepository,
    private val composerRepository: ComposerRepository,

    ) : ViewModel() {


}