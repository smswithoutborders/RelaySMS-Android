package com.example.sw0b_001.data

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result

data class NetworkResponseResults(
    val response: Response,
    val result: Result<String, java.lang.Exception>
)

object Network {
    fun requestGet(url: String, headers: Headers? = null) : NetworkResponseResults{
        try {
            val (_, response, result) = if(headers.isNullOrEmpty())
                url.httpGet()
                    .responseString()
            else
                url.httpGet()
                    .header(Headers.COOKIE to headers["Set-Cookie"].first())
                    .responseString()

            return when(result) {
                is Result.Failure -> {
                    NetworkResponseResults(response, Result.Failure(result.error))
                }

                is Result.Success -> {
                    NetworkResponseResults(response, Result.Success(result.get()))
                }
            }
        } catch(e: Exception) {
            throw Throwable(e)
        }
    }

    fun jsonRequestDelete(url: String, payload: String, headers: Headers? = null) :
            NetworkResponseResults {
        println("url: $url")
        val (_, response, result) = if(headers.isNullOrEmpty())
            Fuel.delete(url)
                .jsonBody(payload)
                .responseString()
        else
            Fuel.delete(url)
                .jsonBody(payload)
                .header(headers)
                .header( Headers.COOKIE to headers["Set-Cookie"].first())
                .responseString()

        return when(result) {
            is Result.Failure -> {
                Log.w(javaClass.name, "Response text - ${String(response.data)}")
                NetworkResponseResults(response, Result.Failure(result.error))
            }

            is Result.Success -> {
                NetworkResponseResults(response, Result.Success(result.get()))
            }
        }
    }
    fun jsonRequestPut(url: String, payload: String, headers: Headers? = null) :
            NetworkResponseResults {
        println("url: $url")
        val (_, response, result) = if(headers.isNullOrEmpty())
            Fuel.put(url)
                .jsonBody(payload)
                .responseString()
        else
            Fuel.put(url)
                .jsonBody(payload)
                .header(headers)
                .header( Headers.COOKIE to headers["Set-Cookie"].first())
                .responseString()

        return when(result) {
            is Result.Failure -> {
                Log.w(javaClass.name, "Response text - ${String(response.data)}")
                NetworkResponseResults(response, Result.Failure(result.error))
            }

            is Result.Success -> {
                NetworkResponseResults(response, Result.Success(result.get()))
            }
        }
    }

    fun jsonRequestPost(
        url: String,
        payload: String,
        headers: Headers? = null
    ) : NetworkResponseResults {
        val (_, response, result) = if(headers.isNullOrEmpty())
            Fuel.post(url)
                .jsonBody(payload)
                .responseString()
        else
            Fuel.post(url)
                .jsonBody(payload)
                .header(headers)
                .header( Headers.COOKIE to headers["Set-Cookie"].first())
                .responseString()


        return when(result) {
            is Result.Failure -> {
                NetworkResponseResults(response, Result.Failure(result.error))
            }

            is Result.Success -> {
                NetworkResponseResults(response, Result.Success(result.get()))
            }
        }
    }
}

