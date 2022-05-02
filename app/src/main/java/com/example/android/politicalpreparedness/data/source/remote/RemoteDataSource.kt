package com.example.android.politicalpreparedness.data.source.remote

import com.example.android.politicalpreparedness.data.Result
import com.example.android.politicalpreparedness.network.models.Election

interface RemoteDataSource {
    suspend fun getElections(): Result<List<Election>>
}