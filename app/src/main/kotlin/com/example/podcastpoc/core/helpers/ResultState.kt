package com.example.podcastpoc.core.helpers

import com.bumptech.glide.load.model.Headers

sealed class ResultState<out R> {
    data class Success<out T>(val data: T, val headers: Headers? = null) : ResultState<T>()
    data class Error(val message: String, val errorCode: Int ?= null) : ResultState<Nothing>()
    object Loading : ResultState<Nothing>()
    object Idle : ResultState<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[message=$message]"
            is Loading -> "Loading"
            is Idle -> "Idle"
        }
    }
}