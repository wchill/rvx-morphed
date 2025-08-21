package app.revanced.patches.music.utils.fix.client

import app.revanced.patches.shared.spoof.useragent.baseSpoofUserAgentPatch
import app.revanced.util.fingerprint.legacyFingerprint
import app.revanced.util.or
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * This is the fingerprint used in the 'client-spoof' patch around 2022.
 * (Integrated into [baseSpoofUserAgentPatch] now.)
 *
 * This method is modified by [baseSpoofUserAgentPatch], so the fingerprint does not check the [Opcode].
 */
internal val userAgentHeaderBuilderFingerprint = legacyFingerprint(
    name = "userAgentHeaderBuilderFingerprint",
    returnType = "Ljava/lang/String;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("(Linux; U; Android "),
)

/**
 * If this flag is activated, a playback issue occurs.
 * (Regardless of the 'Spoof client')
 *
 * Added in YouTube Music 7.33+
 */
internal const val PLAYBACK_FEATURE_FLAG = 45665455L

internal val playbackFeatureFlagFingerprint = legacyFingerprint(
    name = "playbackFeatureFlagFingerprint",
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = emptyList(),
    literals = listOf(PLAYBACK_FEATURE_FLAG),
)