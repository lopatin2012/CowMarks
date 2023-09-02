package com.digital_tent.cow_marks.retrofit

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface WorkshopsJsonAPI {
    @GET("workshops_json")
    suspend fun getWorkshops(): Response<ResponseBody>
}