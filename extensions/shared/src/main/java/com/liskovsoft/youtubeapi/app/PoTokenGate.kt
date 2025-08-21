package com.liskovsoft.youtubeapi.app

import com.liskovsoft.sharedutils.helpers.DeviceHelpers
import com.liskovsoft.youtubeapi.app.potokennp2.PoTokenProviderImpl
import com.liskovsoft.youtubeapi.app.potokennp2.misc.PoTokenResult

internal object PoTokenGate {
    private var mNpPoToken: PoTokenResult? = null
    private var mCacheResetTimeMs: Long = -1
    
    @JvmStatic
    fun getContentPoToken(videoId: String): String? {
        if (!isNpPotSupported()) return null

        if (mNpPoToken?.videoId == videoId) {
            return mNpPoToken?.playerRequestPoToken
        }

        mNpPoToken = if (isNpPotSupported())
            PoTokenProviderImpl.getWebClientPoToken(videoId)
        else null

        return mNpPoToken?.playerRequestPoToken
    }

    @JvmStatic
    fun getSessionPoToken(videoId: String): String? {
        if (!isNpPotSupported()) return null

        if (mNpPoToken != null && mNpPoToken!!.videoId == videoId) {
            val streamingDataPoToken = mNpPoToken!!.streamingDataPoToken
            if (streamingDataPoToken != null) {
                mNpPoToken = null
                return streamingDataPoToken
            }
        }

        mNpPoToken = if (isNpPotSupported())
            PoTokenProviderImpl.getWebClientPoToken(videoId)
        else null

        if (mNpPoToken != null) {
            val streamingDataPoToken = mNpPoToken!!.streamingDataPoToken
            if (streamingDataPoToken != null) {
                mNpPoToken = null
                return streamingDataPoToken
            }
        }

        return null
    }

    @JvmStatic
    fun updatePoToken() {
        if (isNpPotSupported()) {
            //mNpPoToken = null // only refresh
            mNpPoToken = PoTokenProviderImpl.getWebClientPoToken("") // refresh and preload
        }
    }

    @JvmStatic
    fun getVisitorData(): String? {
        if (!isNpPotSupported()) return null

        return mNpPoToken?.visitorData
    }

    @JvmStatic
    fun isNpPotSupported() = DeviceHelpers.isWebViewSupported() && !isWebViewBroken()

    private fun isWebViewBroken(): Boolean = DeviceHelpers.isTCL() // "TCL TV - Harman"

    @JvmStatic
    fun resetCache(): Boolean {
        if (System.currentTimeMillis() < mCacheResetTimeMs) {
            return false
        }

        if (isNpPotSupported()) {
            mNpPoToken = null
        }

        mCacheResetTimeMs = System.currentTimeMillis() + 60_000

        return true
    }
}