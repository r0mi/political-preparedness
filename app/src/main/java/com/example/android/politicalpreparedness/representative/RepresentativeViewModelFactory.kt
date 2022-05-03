package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.data.source.ElectionsRepository

class RepresentativeViewModelFactory(private val electionsRepository: ElectionsRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(ElectionsRepository::class.java)
            .newInstance(electionsRepository)
    }

}
