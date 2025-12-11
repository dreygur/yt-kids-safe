package com.ytkidssafe.video.extractor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request as NewPipeRequest
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.util.concurrent.TimeUnit

/**
 * OkHttp-based Downloader for NewPipe Extractor.
 */
class NewPipeDownloader private constructor() : Downloader() {

    companion object {
        private var instance: NewPipeDownloader? = null

        fun getInstance(): NewPipeDownloader {
            if (instance == null) {
                instance = NewPipeDownloader()
            }
            return instance!!
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    override fun execute(request: NewPipeRequest): Response {
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val requestBuilder = Request.Builder()
            .url(url)
            .method(request.httpMethod(), dataToSend?.toRequestBody())

        // Add headers
        headers.forEach { (key, values) ->
            values.forEach { value ->
                requestBuilder.addHeader(key, value)
            }
        }

        // Add default User-Agent if not present
        if (!headers.containsKey("User-Agent")) {
            requestBuilder.addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            )
        }

        val response = client.newCall(requestBuilder.build()).execute()
        val responseCode = response.code

        // Check for CAPTCHA
        if (responseCode == 429) {
            response.close()
            throw ReCaptchaException("Rate limited", url)
        }

        val responseBody = response.body?.string() ?: ""
        val responseHeaders = response.headers.toMultimap()

        return Response(
            responseCode,
            response.message,
            responseHeaders,
            responseBody,
            response.request.url.toString()
        )
    }
}
