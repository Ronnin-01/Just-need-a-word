package com.bldsht.justneedaword

import com.bldsht.justneedaword.responsemodel.WordResult
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApi {
    @GET("en/{word}")
    suspend fun getMeaning(@Path("word") word : String) : retrofit2.Response<List<WordResult>>
}