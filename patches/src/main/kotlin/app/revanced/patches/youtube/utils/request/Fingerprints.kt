package app.revanced.patches.youtube.utils.request

import app.revanced.util.fingerprint.legacyFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val buildInitPlaybackRequestFingerprint = legacyFingerprint(
    name = "buildInitPlaybackRequestFingerprint",
    returnType = "Lorg/chromium/net/UrlRequest\$Builder;",
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_OBJECT, // Moves the request URI string to a register to build the request with.
    ),
    strings = listOf(
        "Content-Type",
        "Range",
    ),
    customFingerprint = { method, _ ->
        indexOfUriToStringInstruction(method) >= 0 &&
                indexOfMapInstruction(method) >= 0 &&
                !AccessFlags.STATIC.isSet(method.accessFlags)
    },
)

internal fun indexOfMapInstruction(method: Method) =
    method.indexOfFirstInstruction {
        opcode == Opcode.IGET_OBJECT &&
                getReference<FieldReference>()?.type == "Ljava/util/Map;"
    }

internal fun indexOfUriToStringInstruction(method: Method) =
    method.indexOfFirstInstruction {
        opcode == Opcode.INVOKE_VIRTUAL &&
                getReference<MethodReference>().toString() == "Landroid/net/Uri;->toString()Ljava/lang/String;"
    }

internal val buildRequestFingerprint = legacyFingerprint(
    name = "buildRequestFingerprint",
    customFingerprint = { method, _ ->
        method.implementation != null &&
                indexOfRequestFinishedListenerInstruction(method) >= 0 &&
                !method.definingClass.startsWith("Lorg/") &&
                indexOfNewUrlRequestBuilderInstruction(method) >= 0 &&
                // Earlier targets
                (indexOfEntrySetInstruction(method) >= 0 ||
                        // Later targets
                        method.parameters[1].type == "Ljava/util/Map;")
    }
)

internal fun indexOfRequestFinishedListenerInstruction(method: Method) =
    method.indexOfFirstInstruction {
        opcode == Opcode.INVOKE_VIRTUAL &&
                getReference<MethodReference>()?.name == "setRequestFinishedListener"
    }

internal fun indexOfNewUrlRequestBuilderInstruction(method: Method) =
    method.indexOfFirstInstruction {
        opcode == Opcode.INVOKE_VIRTUAL &&
                getReference<MethodReference>().toString() == "Lorg/chromium/net/CronetEngine;->newUrlRequestBuilder(Ljava/lang/String;Lorg/chromium/net/UrlRequest${'$'}Callback;Ljava/util/concurrent/Executor;)Lorg/chromium/net/UrlRequest${'$'}Builder;"
    }

internal fun indexOfEntrySetInstruction(method: Method) =
    method.indexOfFirstInstruction {
        opcode == Opcode.INVOKE_INTERFACE &&
                getReference<MethodReference>().toString() == "Ljava/util/Map;->entrySet()Ljava/util/Set;"
    }