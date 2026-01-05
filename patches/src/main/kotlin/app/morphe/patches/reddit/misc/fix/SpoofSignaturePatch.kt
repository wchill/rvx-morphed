package app.morphe.patches.reddit.misc.fix

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.morphe.patches.reddit.utils.extension.Constants.PATCHES_PATH
import app.morphe.patches.reddit.utils.patch.PatchList.SPOOF_SIGNATURE
import app.morphe.util.fingerprint.mutableClassOrThrow

private const val EXTENSION_CLASS_DESCRIPTOR =
    "$PATCHES_PATH/SpoofSignaturePatch;"

@Suppress("unused")
val spoofSignaturePatch = bytecodePatch(
    SPOOF_SIGNATURE.title,
    SPOOF_SIGNATURE.summary
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    execute {
        applicationFingerprint.mutableClassOrThrow().setSuperClass(EXTENSION_CLASS_DESCRIPTOR)
    }
}
