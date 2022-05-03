package com.example.android.politicalpreparedness.election

import androidx.lifecycle.*
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.SingleLiveEvent
import com.example.android.politicalpreparedness.combineWith
import com.example.android.politicalpreparedness.data.Result
import com.example.android.politicalpreparedness.data.source.ElectionsRepository
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.toVoterInfoAddress
import kotlinx.coroutines.launch

class VoterInfoViewModel(
    private val electionId: Int,
    private val division: Division,
    private val electionsRepository: ElectionsRepository
) : ViewModel() {

    val showErrorMessage: SingleLiveEvent<Pair<String?, Int?>> = SingleLiveEvent()
    val goToUrl: SingleLiveEvent<String> = SingleLiveEvent()

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    private val _election = MutableLiveData<Election?>()
    val election: LiveData<Election?> = _election

    private val _address = MutableLiveData<String?>()
    val address: LiveData<String?> = _address

    private val _ballotInfoUrl = MutableLiveData<String?>()
    val ballotInfoUrl: LiveData<String?> = _ballotInfoUrl

    private val _votingLocationFinderUrl = MutableLiveData<String?>()
    val votingLocationFinderUrl: LiveData<String?> = _votingLocationFinderUrl

    private val following = MutableLiveData(false)

    val followButtonStrResId: LiveData<Int> = Transformations.map(following) {
        if (it == null || !it) {
            R.string.follow_election
        } else {
            R.string.unfollow_election
        }
    }

    val hideOtherElements: LiveData<Boolean> = loading.combineWith(error) { a, b ->
        if (a != null && b != null) {
            a || b
        } else a ?: b!!
    }

    val hideAddressGroup: LiveData<Boolean> = hideOtherElements.combineWith(address) { a, b ->
        (b == null) || a ?: true
    }

    private fun getVoterInfo() {
        _loading.value = true
        viewModelScope.launch {
            try {
                when (val result =
                    electionsRepository.getVoterInfo(division.toVoterInfoAddress(), electionId)) {
                    is Result.Success -> {
                        _loading.postValue(false)
                        _error.postValue(false)
                        _election.postValue(result.data.election)
                        result.data.state?.firstOrNull()?.electionAdministrationBody?.let {
                            it.ballotInfoUrl?.let { url ->
                                _ballotInfoUrl.postValue(url)
                            }
                            it.votingLocationFinderUrl?.let { url ->
                                _votingLocationFinderUrl.postValue(url)
                            }
                            it.correspondenceAddress?.let { address ->
                                _address.postValue(address.toFormattedString())
                            }
                        }
                    }
                    is Result.Error -> {
                        _loading.postValue(false)
                        _error.postValue(true)
                        _ballotInfoUrl.postValue(null)
                        _votingLocationFinderUrl.postValue(null)
                        _address.postValue(null)
                        showErrorMessage.postValue(
                            Pair(
                                result.exception.localizedMessage,
                                R.string.failed_to_load_voter_info
                            )
                        )
                    }
                    Result.Loading -> {
                        _loading.postValue(true)
                    }
                }
            } catch (e: Exception) {
                _loading.postValue(false)
                _error.postValue(true)
                _ballotInfoUrl.postValue(null)
                _votingLocationFinderUrl.postValue(null)
                _address.postValue(null)
                showErrorMessage.postValue(
                    Pair(
                        e.localizedMessage,
                        R.string.failed_to_load_voter_info
                    )
                )
            }
        }
    }

    fun votingLocationClicked() {
        goToUrl.value = votingLocationFinderUrl.value
    }

    fun ballotInfoClicked() {
        goToUrl.value = ballotInfoUrl.value
    }

    fun toggleFollow() {
        viewModelScope.launch {
            election.value?.let { _election ->
                following.value?.let { _following ->
                    if (_following) {
                        electionsRepository.unFollowElection(_election)
                    } else {
                        electionsRepository.followElection(_election)
                    }
                    isElectionFollowed()
                }
            }
        }
    }

    init {
        getVoterInfo()
        isElectionFollowed()
    }

    private fun isElectionFollowed() {
        viewModelScope.launch {
            following.postValue(electionsRepository.isElectionFollowed(electionId))
        }
    }
}