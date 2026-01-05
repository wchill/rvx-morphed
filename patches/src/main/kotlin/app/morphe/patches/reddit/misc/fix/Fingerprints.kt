package app.morphe.patches.reddit.misc.fix

import app.morphe.util.fingerprint.legacyFingerprint
import app.morphe.util.or
import com.android.tools.smali.dexlib2.AccessFlags

internal val applicationFingerprint = legacyFingerprint(
    name = "applicationFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Landroid/content/Context;"),
    customFingerprint = { methodDef, classDef ->
        classDef.superclass == "Landroid/app/Application;" &&
                methodDef.name == "attachBaseContext"
    }
)
