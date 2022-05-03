package com.example.android.politicalpreparedness.election

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.data.source.ElectionsRepository
import com.example.android.politicalpreparedness.network.models.Division

class VoterInfoViewModelFactory(
    private val electionId: Int,
    private val division: Division,
    private val electionsRepository: ElectionsRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            Int::class.java,
            Division::class.java,
            ElectionsRepository::class.java
        )
            .newInstance(electionId, division, electionsRepository)
    }
}