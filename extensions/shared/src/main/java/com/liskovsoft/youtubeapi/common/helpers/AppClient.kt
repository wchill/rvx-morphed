package com.liskovsoft.youtubeapi.common.helpers

private const val JSON_POST_DATA_BASE = "{\"context\":{\"client\":{\"clientName\":\"%s\",\"clientVersion\":\"%s\"," +
        "\"clientScreen\":\"%s\",\"userAgent\":\"%s\",\"browserName\":\"%s\",\"browserVersion\":\"%s\",%s\"acceptLanguage\":\"%%s\",\"acceptRegion\":\"%%s\"," +
        "\"utcOffsetMinutes\":\"%%s\",\"visitorData\":\"%%s\"},%%s\"user\":{\"enableSafetyMode\":false,\"lockedSafetyMode\":false}}," +
        "\"racyCheckOk\":true,\"contentCheckOk\":true,%%s}"
// Merge Shorts with Subscriptions: TV_APP_QUALITY_LIMITED_ANIMATION
// Separate Shorts from Subscriptions: TV_APP_QUALITY_FULL_ANIMATION
private const val POST_DATA_BROWSE =
    "\"tvAppInfo\":{\"appQuality\":\"TV_APP_QUALITY_FULL_ANIMATION\",\"zylonLeftNav\":true},\"webpSupport\":false,\"animatedWebpSupport\":true,"
private const val POST_DATA_BROWSE_LEGACY =
    "\"tvAppInfo\":{\"appQuality\":\"TV_APP_QUALITY_LIMITED_ANIMATION\",\"zylonLeftNav\":true},\"webpSupport\":false,\"animatedWebpSupport\":true,"
private const val POST_DATA_ANDROID = "\"androidSdkVersion\":\"%s\","
private const val CLIENT_SCREEN_WATCH = "WATCH" // won't play 18+ restricted videos
private const val CLIENT_SCREEN_EMBED = "EMBED" // no 18+ restriction but not all video embeddable, and no descriptions

/**
 * https://github.com/gamer191/yt-dlp/blob/3ad3676e585d144c16a2c5945eb6e422fb918d44/yt_dlp/extractor/youtube/_base.py#L41
 */
internal enum class AppClient(
    val clientName: String, val clientVersion: String, val innerTubeName: Int, val userAgent: String, val referer: String?,
    val clientScreen: String = CLIENT_SCREEN_WATCH, val params: String? = null, val postData: String? = null, val postDataBrowse: String? = null
) {
    // 8AEB - premium formats?
    TV("TVHTML5", "7.20250714.16.00", 7, userAgent = DefaultHeaders.USER_AGENT_TV,
        referer = "https://www.youtube.com/tv", params = "8AEB", postDataBrowse = POST_DATA_BROWSE),
    TV_LEGACY(TV, postDataBrowse = POST_DATA_BROWSE_LEGACY),
    TV_EMBED("TVHTML5_SIMPLY_EMBEDDED_PLAYER", "2.0", 85, userAgent = DefaultHeaders.USER_AGENT_TV,
        referer = "https://www.youtube.com/tv", clientScreen = CLIENT_SCREEN_EMBED, postDataBrowse = POST_DATA_BROWSE),
    WEB("WEB", "2.20250312.04.00", 1, userAgent = DefaultHeaders.USER_AGENT_WEB,
        referer = "https://www.youtube.com/", params = "8AEB"),
    // Use WEB_EMBEDDED_PLAYER instead of WEB. Some videos have 403 error on WEB.
    WEB_EMBED("WEB_EMBEDDED_PLAYER", "1.20250310.01.00", 56, userAgent = DefaultHeaders.USER_AGENT_WEB,
        referer = "https://www.youtube.com/"),
    MWEB("MWEB", "2.20250213.05.00", 2, userAgent = DefaultHeaders.USER_AGENT_MOBILE_WEB,
        referer = "https://m.youtube.com/"),
    ANDROID("ANDROID", "19.26.37", 3, userAgent = DefaultHeaders.USER_AGENT_ANDROID,
        referer = null, postData = String.format(POST_DATA_ANDROID, 30)),
    ANDROID_VR("ANDROID_VR", "1.37", 28, userAgent = DefaultHeaders.USER_AGENT_WEB,
        referer = "https://www.youtube.com/"),
    INITIAL(TV);

    constructor(baseClient: AppClient, postData: String? = null, postDataBrowse: String? = null): this(baseClient.clientName, baseClient.clientVersion,
        baseClient.innerTubeName, baseClient.userAgent, baseClient.referer, baseClient.clientScreen, baseClient.params,
        postData ?: baseClient.postData, postDataBrowse ?: baseClient.postDataBrowse)

    private val browserInfo by lazy { extractBrowserInfo(userAgent) }
    val browserName by lazy { browserInfo.first }
    val browserVersion by lazy { browserInfo.second }
    val browseTemplate by lazy { String.format(JSON_POST_DATA_BASE, clientName, clientVersion, clientScreen, userAgent,
        browserName, browserVersion, (postData ?: "") + (postDataBrowse ?: "")) }
    val playerTemplate by lazy { String.format(JSON_POST_DATA_BASE, clientName, clientVersion, clientScreen, userAgent,
        browserName, browserVersion, (postData ?: "")) }

    fun isAuthSupported() = this == TV || this == TV_EMBED // NOTE: TV_SIMPLE doesn't support auth
    fun isPotSupported() = this == WEB || this == MWEB || this == WEB_EMBED || this == ANDROID_VR

    private fun extractBrowserInfo(userAgent: String): Pair<String, String> {
        // Include Shorts: "browserName":"Cobalt"
        val browserName = "Cobalt"
        val browserVersion = "22.lts.3.306369-gold"

        for (name in listOf("SamsungBrowser", "LG Browser", "Cobalt", "Chrome", "Safari")) {
            val version = extractBrowserVersion(userAgent, name)
            if (version != null)
                return Pair(name, version)
        }

        return Pair(browserName, browserVersion)
    }

    private fun extractBrowserVersion(userAgent: String, name: String): String? {
        if (userAgent.contains(name, ignoreCase = true)) {
            val browserVersionMatch = "$name/([a-zA-Z0-9.-]+)".toRegex().find(userAgent)
            return browserVersionMatch?.groupValues?.getOrNull(1)
        }

        return null
    }
}