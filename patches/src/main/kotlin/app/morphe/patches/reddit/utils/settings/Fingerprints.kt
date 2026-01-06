package app.morphe.patches.reddit.utils.settings

import app.morphe.patches.reddit.utils.extension.Constants.EXTENSION_PATH
import app.morphe.util.fingerprint.legacyFingerprint
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstruction
import app.morphe.util.or
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

internal val preferenceDestinationFingerprint = legacyFingerprint(
    name = "preferenceDestinationFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Lcom/reddit/domain/settings/Destination;"),
    opcodes = listOf(
        Opcode.IGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
    ),
    strings = listOf("settingIntentProvider"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.startsWith("Lcom/reddit/screen/settings/preferences/")
    }
)

internal val preferenceManagerFingerprint = legacyFingerprint(
    name = "preferenceManagerFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    customFingerprint = { methodDef, _ ->
        fun indexOfPreferencesPresenterInstruction(methodDef: Method) =
            methodDef.indexOfFirstInstruction {
                opcode == Opcode.NEW_INSTANCE &&
                        getReference<TypeReference>()?.type?.contains("checkIfShouldShowImpressumOption") == true
            }
        indexOfPreferencesPresenterInstruction(methodDef) >= 0
    }
)

internal val preferenceManagerParentFingerprint = legacyFingerprint(
    name = "preferenceManagerParentFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Landroid/os/Bundle;"),
    strings = listOf("prefs_share_contacts_painted_door")
)

internal val redditInternalFeaturesFingerprint = legacyFingerprint(
    name = "redditInternalFeaturesFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    strings = listOf("RELEASE"),
    customFingerprint = { methodDef, _ ->
        !methodDef.definingClass.startsWith("Lcom/")
    }
)

internal val webBrowserActivityOnCreateFingerprint = legacyFingerprint(
    name = "webBrowserActivityOnCreateFingerprint",
    returnType = "V",
    strings = listOf("com.reddit.extra.initial_url"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/WebBrowserActivity;") &&
                methodDef.name == "onCreate"
    }
)

internal val settingsStatusLoadFingerprint = legacyFingerprint(
    name = "settingsStatusLoadFingerprint",
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("$EXTENSION_PATH/settings/SettingsStatus;") &&
                methodDef.name == "load"
    }
)