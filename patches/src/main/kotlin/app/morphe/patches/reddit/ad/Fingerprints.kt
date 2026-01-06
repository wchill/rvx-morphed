package app.morphe.patches.reddit.ad

import app.morphe.util.fingerprint.legacyFingerprint
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstruction
import app.morphe.util.or
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

internal val listingFingerprint = legacyFingerprint(
    name = "listingFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    opcodes = listOf(
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT
    ),
    // "children" are present throughout multiple versions
    strings = listOf(
        "children",
        "uxExperiences"
    ),
    customFingerprint = { _, classDef ->
        classDef.type.endsWith("/Listing;")
    },
)

internal val submittedListingFingerprint = legacyFingerprint(
    name = "submittedListingFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    opcodes = listOf(
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT
    ),
    // "children" are present throughout multiple versions
    strings = listOf(
        "children",
        "videoUploads"
    ),
    customFingerprint = { _, classDef ->
        classDef.type.endsWith("/SubmittedListing;")
    },
)

internal val adPostSectionConstructorFingerprint = legacyFingerprint(
    name = "adPostSectionConstructorFingerprint",
    returnType = "V",
    strings = listOf("sections"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "<init>"
    }
)

internal val adPostSectionToStringFingerprint = legacyFingerprint(
    name = "adPostSectionToStringFingerprint",
    returnType = "Ljava/lang/String;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = emptyList(),
    strings = listOf(
        "AdPostSection(linkId=",
        ", sections=",
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "toString"
    }
)

internal val commentAdCommentScreenAdViewFingerprint = legacyFingerprint(
    name = "commentAdCommentScreenAdViewFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L"),
    strings = listOf("ad"),
    customFingerprint = { _, classDef ->
        classDef.type.endsWith("/CommentScreenAdView;")
    },
)

internal val commentAdDetailListHeaderViewFingerprint = legacyFingerprint(
    name = "commentAdDetailListHeaderViewFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L"),
    strings = listOf("ad"),
    customFingerprint = { _, classDef ->
        classDef.type.endsWith("/DetailListHeaderView;")
    },
)

internal val commentsViewModelFingerprint = legacyFingerprint(
    name = "commentsViewModelFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L", "Z", "L", "I"),
    customFingerprint = { method, classDef ->
        classDef.superclass == "Lcom/reddit/screen/presentation/CompositionViewModel;" &&
                method.indexOfFirstInstruction {
                    opcode == Opcode.NEW_INSTANCE &&
                            getReference<TypeReference>()?.type?.startsWith("Lcom/reddit/postdetail/comment/refactor/CommentsViewModel\$LoadAdsSeparately\$") == true
                } >= 0
    },
)

internal val commentsViewModelConstructorFingerprint = legacyFingerprint(
    name = "commentsViewModelConstructorFingerprint",
    returnType = "V",
    customFingerprint = { methodDef, classDef ->
        classDef.superclass == "Lcom/reddit/screen/presentation/CompositionViewModel;" &&
                methodDef.definingClass.endsWith("/CommentsViewModel;") &&
                methodDef.name == "<init>"
    },
)

internal val immutableListBuilderFingerprint = legacyFingerprint(
    name = "immutableListBuilderFingerprint",
    returnType = "V",
    parameters = emptyList(),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "<clinit>" &&
                indexOfAutoplayVideoPreviewsOptionInstruction(methodDef) >= 0 &&
                indexOfImmutableListBuilderInstruction(methodDef) >= 0
    }
)

internal val postDetailAdLoaderFingerprint = legacyFingerprint(
    name = "postDetailAdLoaderFingerprint",
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.contains("/RedditPostDetailAdLoader\$loadPostDetailAds\$")
                && methodDef.name == "invokeSuspend"
    }
)

internal fun indexOfAddArrayListInstruction(method: Method, index: Int = 0) =
    method.indexOfFirstInstruction(index) {
        getReference<MethodReference>()?.toString() == "Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z"
    }

internal fun indexOfAutoplayVideoPreviewsOptionInstruction(methodDef: Method) =
    methodDef.indexOfFirstInstruction {
        val reference = getReference<MethodReference>()
        opcode == Opcode.INVOKE_STATIC &&
                reference?.name == "getEntries" &&
                reference.definingClass == "Lcom/reddit/accessibility/AutoplayVideoPreviewsOption;"
    }

internal fun indexOfImmutableListBuilderInstruction(methodDef: Method) =
    methodDef.indexOfFirstInstruction {
        val reference = getReference<MethodReference>()
        opcode == Opcode.INVOKE_STATIC &&
                reference?.parameterTypes?.size == 1 &&
                reference.parameterTypes.firstOrNull() == "Ljava/lang/Iterable;"
    }