package app.morphe.patches.reddit.layout.sidebar

import app.morphe.util.fingerprint.legacyFingerprint
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstruction
import app.morphe.util.or
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val communityDrawerPresenterConstructorFingerprint = legacyFingerprint(
    name = "communityDrawerPresenterConstructorFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    strings = listOf("communityDrawerSettings"),
    customFingerprint = { methodDef, _ ->
        indexOfHeaderItemInstruction(methodDef) >= 0
    }
)

internal val communityDrawerPresenterFingerprint = legacyFingerprint(
    name = "communityDrawerPresenterFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = emptyList(),
    opcodes = listOf(
        Opcode.XOR_INT_2ADDR,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
    ),
    customFingerprint = { methodDef, _ ->
        indexOfKotlinCollectionInstruction(methodDef) >= 0
    }
)

internal fun indexOfKotlinCollectionInstruction(
    methodDef: Method,
    startIndex: Int = 0
) = methodDef.indexOfFirstInstruction(startIndex) {
    val reference = getReference<MethodReference>()
    opcode == Opcode.INVOKE_STATIC &&
            reference?.returnType == "Ljava/util/ArrayList;" &&
            reference.definingClass.startsWith("Lkotlin/collections/") &&
            reference.parameterTypes.size == 2 &&
            reference.parameterTypes[0].toString() == "Ljava/lang/Iterable;" &&
            reference.parameterTypes[1].toString() == "Ljava/util/Collection;"
}

internal val redditProLoaderFingerprint = legacyFingerprint(
    name = "redditProLoaderFingerprint",
    returnType = "Ljava/lang/Object;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    customFingerprint = { methodDef, _ ->
        methodDef.parameterTypes.firstOrNull() == "Ljava/lang/Object;" &&
                indexOfHeaderItemInstruction(methodDef, "REDDIT_PRO") >= 0
    }
)

internal fun indexOfHeaderItemInstruction(
    methodDef: Method,
    fieldName: String = "RECENTLY_VISITED",
) = methodDef.indexOfFirstInstruction {
    getReference<FieldReference>()?.name == fieldName
}

internal val sidebarComponentsPatchFingerprint = legacyFingerprint(
    name = "sidebarComponentsPatchFingerprint",
    returnType = "Ljava/lang/String;",
    accessFlags = AccessFlags.PRIVATE or AccessFlags.STATIC,
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/SidebarComponentsPatch;") &&
                methodDef.name == "getHeaderItemName"
    }
)

internal val headerItemUiModelToStringFingerprint = legacyFingerprint(
    name = "headerItemUiModelToStringFingerprint",
    returnType = "Ljava/lang/String;",
    strings = listOf(
        "HeaderItemUiModel(uniqueId=",
        ", type="
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "toString"
    }
)
