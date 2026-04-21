package com.ud23.identifi.domain.model

data class User(
    val id: String,
    val name: String,
    val faceVector: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as User
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
