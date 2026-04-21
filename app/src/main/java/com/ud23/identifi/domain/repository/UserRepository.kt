package com.ud23.identifi.domain.repository

import com.ud23.identifi.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun saveUser(user: User)
    suspend fun deleteUser(userId: String)
    suspend fun getUserById(userId: String): User?
    fun getAllUsers(): Flow<List<User>>
}