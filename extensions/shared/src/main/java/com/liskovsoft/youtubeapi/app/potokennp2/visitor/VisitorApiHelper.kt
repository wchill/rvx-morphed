package com.liskovsoft.youtubeapi.app.potokennp2.visitor

import app.revanced.extension.shared.utils.Utils
import com.liskovsoft.youtubeapi.common.helpers.AppClient
import com.liskovsoft.youtubeapi.common.helpers.QueryBuilder
import com.liskovsoft.youtubeapi.common.helpers.PostDataType
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal object VisitorApiHelper {
    fun getVisitorQuery(): String {
        val locale: Locale = Utils.getContext().resources.configuration.locale
        val country = locale.country
        val language = locale.toLanguageTag()
        val tz: TimeZone = TimeZone.getDefault()
        val now = Date()
        val utcOffsetMinute = tz.getOffset(now.time) / 1_000 / 60
        return QueryBuilder(AppClient.WEB)
            .setType(PostDataType.Default)
            .setLanguage(language)
            .setCountry(country)
            .setUtcOffsetMinutes(utcOffsetMinute)
            .build()
    }
}