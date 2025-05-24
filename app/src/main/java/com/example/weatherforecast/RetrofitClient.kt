package com.example.weatherforecast

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: RetrofitClient? = null

        fun getInstance(): RetrofitClient {
            return INSTANCE ?: synchronized(this) {
                val instance = RetrofitClient()
                INSTANCE = instance
                instance
            }
        }
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}