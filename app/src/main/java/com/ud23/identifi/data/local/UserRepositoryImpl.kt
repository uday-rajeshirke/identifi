package com.ud23.identifi.data.local

import com.ud23.identifi.domain.model.User
import com.ud23.identifi.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun saveUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    override suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)?.toDomain()
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { list ->
            list.map { it.toDomain() }
        }
    }

    // --- Mappers ---
    private fun User.toEntity() = UserEntity(
        id = id,
        name = name,
        faceVector = faceVector.joinToString(",")
    )

    private fun UserEntity.toDomain() = User(
        id = id,
        name = name,
        faceVector = faceVector.split(",").map { it.toFloat() }.toFloatArray()
    )
}