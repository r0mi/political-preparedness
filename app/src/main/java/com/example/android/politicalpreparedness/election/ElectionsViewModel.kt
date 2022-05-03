package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.SingleLiveEvent
import com.example.android.politicalpreparedness.data.Result
import com.example.android.politicalpreparedness.data.source.ElectionsRepository
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch

class ElectionsViewModel(private val electionsRepository: ElectionsRepository) : ViewModel() {

    val showErrorMessage: SingleLiveEvent<Pair<String?, Int?>> = SingleLiveEvent()
    val navigateTo: SingleLiveEvent<NavDirections> = SingleLiveEvent()

    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElection: LiveData<List<Election>?> = _upcomingElections

    private val _upcomingElectionsLoading = MutableLiveData<Boolean>()
    val upcomingElectionsLoading: LiveData<Boolean> = _upcomingElectionsLoading

    val savedElections: LiveData<List<Election>> = electionsRepository.observeFollowedElections()

    init {
        refreshUpcomingElections()
    }

    private fun refreshUpcomingElections() {
        _upcomingElectionsLoading.value = true
        viewModelScope.launch {
            try {
                when (val result = electionsRepository.getElections()) {
                    is Result.Success -> {
                        val elections = result.data
                        _upcomingElections.value = elections
                        _upcomingElectionsLoading.postValue(false)
                    }
                    is Result.Error -> {
                        showErrorMessage.postValue(
                            Pair(
                                result.exception.localizedMessage,
                                R.string.failed_to_load_upcoming_elections
                            )
                        )
                        _upcomingElectionsLoading.postValue(false)
                    }
                    is Result.Loading -> {
                        _upcomingElectionsLoading.postValue(true)
                    }
                }
            } catch (e: Exception) {
                _upcomingElectionsLoading.postValue(false)
                showErrorMessage.postValue(
                    Pair(
                        e.localizedMessage,
                        R.string.failed_to_load_upcoming_elections
                    )
                )
            }
        }
    }

    fun electionClicked(election: Election) {
        navigateTo.value = ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(
            election.id,
            election.division
        )
    }
}