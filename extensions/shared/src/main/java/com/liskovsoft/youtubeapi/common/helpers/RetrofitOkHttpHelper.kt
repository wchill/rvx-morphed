package com.liskovsoft.youtubeapi.common.helpers

import app.revanced.extension.shared.innertube.utils.ThrottlingParameterUtils
import com.liskovsoft.sharedutils.okhttp.OkHttpCommons
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale

internal object RetrofitOkHttpHelper {
    private const val API_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"
    private const val SEARCH_API_URL: String =
        "https://clients1.google.com/complete/search?client=youtube-lr&ds=yt&xhr=t&oe=utf-8&xssi=t"
    private val authSkipList = mutableListOf<Request>()

    @JvmStatic
    val authHeaders = mutableMapOf<String, String>()

    @JvmStatic
    val authHeaders2 = mutableMapOf<String, String>()

    @JvmStatic
    val client: OkHttpClient by lazy { createClient() }

    @JvmStatic
    var disableCompression: Boolean = false

    @JvmStatic
    fun addAuthSkip(request: Request) {
        if (!authSkipList.contains(request))
            authSkipList.add(request)
    }

    private val commonHeaders = mapOf(
        // Enable compression in production
        "Accept-Encoding" to DefaultHeaders.ACCEPT_ENCODING,
    )

    private val apiHeaders = mapOf(
        "User-Agent" to DefaultHeaders.APP_USER_AGENT,
        "Referer" to DefaultHeaders.REFERER
    )

    private val apiPrefixes = arrayOf(
        "https://m.youtube.com/youtubei/v1/",
        "https://www.youtube.com/youtubei/v1/",
        "https://www.youtube.com/api/stats/",
        "https://clients1.google.com/complete/"
    )

    private fun createClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        addCommonHeaders(builder)
        OkHttpCommons.setupBuilder(builder)
        return builder.build()
    }

    private fun addCommonHeaders(builder: OkHttpClient.Builder) {
        builder.addInterceptor { chain ->
            val request = chain.request()
            val headers = request.headers
            val requestBuilder = request.newBuilder()

            applyHeaders(this.commonHeaders, headers, requestBuilder)

            val url = request.url.toString()

            if (startsWithAny(url, *apiPrefixes)) {
                val doSkipAuth = authSkipList.remove(request)

                // Empty Home fix (anonymous user) and improve Recommendations for everyone
                headers["X-Goog-Visitor-Id"] ?: ThrottlingParameterUtils.getVisitorId()?.let { requestBuilder.header("X-Goog-Visitor-Id", it) }

                if (doSkipAuth) // visitor generation fix
                    requestBuilder.removeHeader("X-Goog-Visitor-Id")

                applyHeaders(this.apiHeaders, headers, requestBuilder)

                if (authHeaders.isEmpty() || doSkipAuth) {
                    applyQueryKeys(mapOf("key" to API_KEY, "prettyPrint" to "false"), request, requestBuilder)
                } else {
                    applyQueryKeys(mapOf("prettyPrint" to "false"), request, requestBuilder)
                    // Fix suggestions on non branded accounts
                    if (url.startsWith(SEARCH_API_URL) && authHeaders2.isNotEmpty()) {
                        applyHeaders(authHeaders2, headers, requestBuilder)
                    } else {
                        applyHeaders(authHeaders, headers, requestBuilder)
                    }
                }
            }

            chain.proceed(requestBuilder.build())
        }
    }

    private fun applyHeaders(newHeaders: Map<String, String?>, oldHeaders: Headers, builder: Request.Builder) {
        for (header in newHeaders) {
            if (disableCompression && header.key == "Accept-Encoding") {
                continue
            }

            // Don't override existing headers
            oldHeaders[header.key] ?: header.value?.let { builder.header(header.key, it) } // NOTE: don't remove null check
        }
    }

    private fun applyQueryKeys(keys: Map<String, String>, request: Request, builder: Request.Builder) {
        val originUrl = request.url

        var newUrlBuilder: HttpUrl.Builder? = null

        for (entry in keys) {
            // Don't override existing keys
            originUrl.queryParameter(entry.key) ?: run {
                if (newUrlBuilder == null) {
                    newUrlBuilder = originUrl.newBuilder()
                }

                newUrlBuilder?.addQueryParameter(entry.key, entry.value)
            }
        }

        newUrlBuilder?.run {
            builder.url(build())
        }
    }

    private fun startsWithAny(word: String?, vararg prefixes: String?): Boolean {
        if (word == null) {
            return false
        }

        for (prefix in prefixes) {
            if (startsWith(word, prefix)) {
                return true
            }
        }

        return false
    }

    private fun startsWith(word: String?, prefix: String?): Boolean {
        var word = word
        var prefix = prefix
        if (word == null && prefix == null) {
            return true
        }

        if (word == null || prefix == null) {
            return false
        }

        word = normalize(word)
        prefix = normalize(prefix)

        return word!!.startsWith(prefix!!)
    }

    private fun normalize(word: String?): String? {
        if (word == null || word.isEmpty()) {
            return word
        }

        return word.lowercase(Locale.getDefault()).replace("ё", "е")
    }
}