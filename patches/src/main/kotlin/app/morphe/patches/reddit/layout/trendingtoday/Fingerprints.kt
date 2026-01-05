package app.morphe.patches.reddit.layout.trendingtoday

import app.morphe.util.fingerprint.legacyFingerprint
import app.morphe.util.or
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val searchTypeaheadListDefaultPresentationConstructorFingerprint = legacyFingerprint(
    name = "searchTypeaheadListDefaultPresentationConstructorFingerprint",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "<init>"
    }
)

internal val searchTypeaheadListDefaultPresentationToStringFingerprint = legacyFingerprint(
    name = "searchTypeaheadListDefaultPresentationToStringFingerprint",
    returnType = "Ljava/lang/String;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = emptyList(),
    strings = listOf("OnSearchTypeaheadListDefaultPresentation(title="),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "toString"
    }
)

internal val trendingTodayTitleFingerprint = legacyFingerprint(
    name = "trendingTodayTitleFingerprint",
    opcodes = listOf(Opcode.AND_INT_LIT8),
    strings = listOf("trending_today_title"),
    customFingerprint = { _, classDef ->
        classDef.type.startsWith("Lcom/reddit/") &&
                classDef.type.contains("/composables/")
    },
)

internal val trendingTodayItemFingerprint = legacyFingerprint(
    name = "trendingTodayItemFingerprint",
    returnType = "V",
    strings = listOf("search_trending_item"),
    customFingerprint = { _, classDef ->
        classDef.type.startsWith("Lcom/reddit/search/combined/ui/composables")
    },
)

internal val trendingTodayItemLegacyFingerprint = legacyFingerprint(
    name = "trendingTodayItemLegacyFingerprint",
    returnType = "V",
    strings = listOf("search_trending_item"),
    customFingerprint = { _, classDef ->
        classDef.type.startsWith("Lcom/reddit/typeahead/ui/zerostate/composables")
    },
)
