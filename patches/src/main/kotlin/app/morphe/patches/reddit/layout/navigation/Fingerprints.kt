package app.morphe.patches.reddit.layout.navigation

import app.morphe.util.containsLiteralInstruction
import app.morphe.util.fingerprint.legacyFingerprint
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstruction
import app.morphe.util.or
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import kotlin.compareTo
import kotlin.or
import kotlin.toString

internal val bottomNavScreenFingerprint = legacyFingerprint(
    name = "bottomNavScreenFingerprint",
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Landroid/content/res/Resources;"),
    strings = listOf("answersFeatures"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lcom/reddit/launch/bottomnav/BottomNavScreen;" &&
                indexOfListBuilderInstruction(methodDef) >= 0
    }
)

fun indexOfGetDimensionPixelSizeInstruction(methodDef: Method) =
    methodDef.indexOfFirstInstruction {
        opcode == Opcode.INVOKE_VIRTUAL &&
                getReference<MethodReference>()?.toString() == "Landroid/content/res/Resources;->getDimensionPixelSize(I)I"
    }

fun indexOfListBuilderInstruction(methodDef: Method) =
    methodDef.indexOfFirstInstruction {
        opcode == Opcode.INVOKE_VIRTUAL &&
                getReference<MethodReference>()?.toString() == "Lkotlin/collections/builders/ListBuilder;->build()Ljava/util/List;"
    }

internal val bottomNavScreenHandlerFingerprint = legacyFingerprint(
    name = "bottomNavScreenHandlerFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L", "L", "Z", "Landroid/view/ViewGroup;", "L"),
    customFingerprint = { method, _ ->
        indexOfGetItemsInstruction(method) >= 0 &&
                indexOfSetSelectedItemTypeInstruction(method) >= 0
    }
)

fun indexOfGetItemsInstruction(method: Method) =
    method.indexOfFirstInstruction {
        opcode == Opcode.INVOKE_VIRTUAL &&
                getReference<MethodReference>()?.name == "getItems"
    }

fun indexOfSetSelectedItemTypeInstruction(method: Method) =
    method.indexOfFirstInstruction {
        opcode == Opcode.INVOKE_VIRTUAL &&
                getReference<MethodReference>()?.name == "setSelectedItemType"
    }

internal val bottomNavScreenOnGlobalLayoutFingerprint = legacyFingerprint(
    name = "bottomNavScreenOnGlobalLayoutFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = emptyList(),
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    ),
    customFingerprint = { method, _ ->
        method.name == "onGlobalLayout"
    }
)

private const val CHAT_BUTTON_MAGIC_NUMBER = 1906671695L

internal val bottomNavScreenSetupBottomNavigationFingerprint = legacyFingerprint(
    name = "bottomNavScreenSetupBottomNavigationFingerprint",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    opcodes = listOf(Opcode.FILLED_NEW_ARRAY),
    customFingerprint = { method, classDef ->
        method.containsLiteralInstruction(CHAT_BUTTON_MAGIC_NUMBER) &&
                method.name == "invoke" &&
                indexOfButtonsArrayInstruction(method) >= 0
    }
)

internal val composeBottomNavScreenFingerprint = legacyFingerprint(
    name = "composeBottomNavScreenFingerprint",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Landroid/content/res/Resources;"),
    opcodes = listOf(Opcode.FILLED_NEW_ARRAY),
    customFingerprint = { method, classDef ->
        classDef.type == "Lcom/reddit/launch/bottomnav/ComposeBottomNavScreen;" &&
                indexOfButtonsArrayInstruction(method) >= 0
    }
)

internal fun indexOfButtonsArrayInstruction(method: Method) =
    method.indexOfFirstInstruction {
        opcode == Opcode.FILLED_NEW_ARRAY &&
                getReference<TypeReference>()?.type?.startsWith("[Lcom/reddit/widget/bottomnav/") == true
    }
