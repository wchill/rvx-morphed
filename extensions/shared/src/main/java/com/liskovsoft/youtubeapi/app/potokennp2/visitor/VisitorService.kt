package com.liskovsoft.youtubeapi.app.potokennp2.visitor

import com.liskovsoft.youtubeapi.app.potokennp2.visitor.data.getVisitorData
import com.liskovsoft.youtubeapi.common.helpers.RetrofitHelper

internal object VisitorService {
    private val mApi = RetrofitHelper.create(VisitorApi::class.java)
    fun getVisitorData(): String? {
        val visitorResult = RetrofitHelper.get(mApi.getVisitorId(), true)

        return visitorResult?.getVisitorData()
    }
}