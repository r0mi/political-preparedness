package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.SingleLiveEvent
import com.example.android.politicalpreparedness.data.Result
import com.example.android.politicalpreparedness.data.source.ElectionsRepository
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel(private val electionsRepository: ElectionsRepository) : ViewModel() {

    val showErrorMessage: SingleLiveEvent<Pair<String?, Int?>> = SingleLiveEvent()
    val requestLocation: SingleLiveEvent<Unit> = SingleLiveEvent()
    val findRepresentatives: SingleLiveEvent<Unit> = SingleLiveEvent()

    private val _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean> = _showLoading

    private val _address = MutableLiveData<Address>()
    val address: LiveData<Address> = _address

    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>?> = _representatives

    val addressLine1 = MutableLiveData("")
    val addressLine2 = MutableLiveData("")
    val city = MutableLiveData("")
    val state = MutableLiveData("")
    val zip = MutableLiveData("")

    private fun fetchRepresentatives(address: Address) {
        _showLoading.value = true
        viewModelScope.launch {
            try {
                when (val result =
                    electionsRepository.getRepresentatives(address.toFormattedString())) {
                    is Result.Success -> {
                        val (offices, officials) = result.data
                        _representatives.value =
                            offices.flatMap { office -> office.getRepresentatives(officials) }
                        _showLoading.postValue(false)
                    }
                    is Result.Error -> {
                        showErrorMessage.postValue(
                            Pair(
                                result.exception.localizedMessage,
                                R.string.failed_to_load_representatives
                            )
                        )
                        _showLoading.postValue(false)
                    }
                    Result.Loading -> {
                        _showLoading.postValue(true)
                    }
                }
            } catch (e: Exception) {
                _showLoading.postValue(false)
                showErrorMessage.postValue(
                    Pair(
                        e.localizedMessage,
                        R.string.failed_to_load_representatives
                    )
                )
            }
        }
    }

    fun setAddress(address: Address) {
        _address.value = address
        addressLine1.value = address.line1
        addressLine2.value = address.line2
        city.value = address.city
        state.value = address.state
        zip.value = address.zip
    }

    private fun getAddress(): Address? {
        return when {
            addressLine1.value.isNullOrBlank() -> {
                showErrorMessage.value = Pair(null, R.string.error_address_line1)
                null
            }
            city.value.isNullOrBlank() -> {
                showErrorMessage.value = Pair(null, R.string.error_city)
                null
            }
            state.value.isNullOrBlank() -> {
                showErrorMessage.value = Pair(null, R.string.error_state)
                null
            }
            zip.value.isNullOrBlank() -> {
                showErrorMessage.value = Pair(null, R.string.error_zip)
                null
            }
            else -> {
                Address(
                    addressLine1.value!!,
                    addressLine2.value,
                    city.value!!,
                    state.value!!,
                    zip.value!!
                )
            }
        }
    }

    fun getLocation() {
        requestLocation.postValue(Unit)
    }

    fun findRepresentativesClicked() {
        getAddress()?.let {
            findRepresentatives.postValue(Unit)
            fetchRepresentatives(it)
        }
    }
}
