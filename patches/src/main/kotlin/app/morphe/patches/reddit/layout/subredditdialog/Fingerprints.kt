package app.morphe.patches.reddit.layout.subredditdialog

import app.morphe.patches.reddit.utils.resourceid.nsfwDialogTitle
import app.morphe.util.fingerprint.legacyFingerprint
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstruction
import app.morphe.util.or
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val frequentUpdatesSheetScreenFingerprint = legacyFingerprint(
    name = "frequentUpdatesSheetScreenFingerprint",
    returnType = "Landroid/view/View;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    opcodes = listOf(
        Opcode.CONST,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.IF_EQZ
    ),
    customFingerprint = { _, classDef ->
        classDef.type == "Lcom/reddit/screens/pager/FrequentUpdatesSheetScreen;"
    }
)

internal val frequentUpdatesHandlerFingerprint = legacyFingerprint(
    name = "frequentUpdatesHandlerFingerprint",
    returnType = "Ljava/lang/Object;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf("subreddit_name"),
    customFingerprint = { method, classDef ->
        classDef.type.startsWith("Lcom/reddit/screens/pager/FrequentUpdatesHandler${'$'}handleFrequentUpdates${'$'}") &&
                method.name == "invokeSuspend" &&
                listOfUserIsSubscriberInstruction(method).isNotEmpty()
    }
)

internal fun listOfUserIsSubscriberInstruction(method: Method) =
    method.implementation?.instructions
        ?.withIndex()
        ?.filter { (_, instruction) ->
            val reference = (instruction as? ReferenceInstruction)?.reference
            instruction.opcode == Opcode.INVOKE_INTERFACE &&
                    reference is MethodReference &&
                    reference.name == "getUserIsSubscriber" &&
                    reference.returnType == "Ljava/lang/Boolean;"
        }
        ?.map { (index, _) -> index }
        ?.reversed()
        ?: emptyList()

internal val nsfwAlertEmitFingerprint = legacyFingerprint(
    name = "nsfwAlertEmitFingerprint",
    returnType = "Ljava/lang/Object;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf("nsfwAlertDelegate"),
    customFingerprint = { method, classDef ->
        classDef.type.startsWith("Lcom/reddit/screens/pager/v2/") &&
                method.name == "emit" &&
                indexOfGetOver18Instruction(method) >= 0 &&
                indexOfHasBeenVisitedInstruction(method) >= 0 &&
                indexOfIsIncognitoInstruction(method) >= 0
    }
)

internal fun indexOfGetOver18Instruction(methodDef: Method) =
    methodDef.indexOfFirstInstruction {
        val reference = getReference<MethodReference>()
        opcode == Opcode.INVOKE_VIRTUAL &&
                reference?.name == "getOver18" &&
                reference.returnType == "Ljava/lang/Boolean;"
    }

internal fun indexOfIsIncognitoInstruction(methodDef: Method) =
    methodDef.indexOfFirstInstruction {
        val reference = getReference<MethodReference>()
        opcode == Opcode.INVOKE_INTERFACE &&
                reference?.name == "isIncognito" &&
                reference.returnType == "Z"
    }

internal fun indexOfHasBeenVisitedInstruction(method: Method) =
    method.indexOfFirstInstruction {
        val reference = getReference<MethodReference>()
        opcode == Opcode.INVOKE_VIRTUAL &&
                reference?.name == "getHasBeenVisited" &&
                reference.returnType == "Z"
    }

internal val nsfwAlertBuilderFingerprint = legacyFingerprint(
    name = "nsfwAlertBuilderFingerprint",
    literals = listOf(nsfwDialogTitle),
    customFingerprint = { method, _ ->
        method.definingClass.startsWith("Lcom/reddit/screen/nsfw")
    }
)

internal val redditAlertDialogsFingerprint = legacyFingerprint(
    name = "redditAlertDialogsFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    customFingerprint = { method, _ ->
        method.definingClass.startsWith("Lcom/reddit/screen/dialog/") &&
                indexOfSetBackgroundTintListInstruction(method) >= 0
    }
)

fun indexOfSetBackgroundTintListInstruction(method: Method) =
    method.indexOfFirstInstruction {
        opcode == Opcode.INVOKE_VIRTUAL &&
                getReference<MethodReference>()?.name == "setBackgroundTintList"
    }