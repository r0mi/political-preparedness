package com.example.android.politicalpreparedness.data.source

import com.example.android.politicalpreparedness.data.Result
import com.example.android.politicalpreparedness.network.models.Election

interface ElectionsRepository {
    suspend fun getElections(): Result<List<Election>>

}