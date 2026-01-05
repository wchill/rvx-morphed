package app.morphe.patches.reddit.ad

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patches.reddit.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.morphe.patches.reddit.utils.extension.Constants.PATCHES_PATH
import app.morphe.patches.reddit.utils.patch.PatchList.HIDE_ADS
import app.morphe.patches.reddit.utils.settings.is_2025_06_or_greater
import app.morphe.patches.reddit.utils.settings.is_2025_40_or_greater
import app.morphe.patches.reddit.utils.settings.settingsPatch
import app.morphe.patches.reddit.utils.settings.updatePatchStatus
import app.morphe.util.findMutableMethodOf
import app.morphe.util.fingerprint.methodOrThrow
import app.morphe.util.fingerprint.mutableClassOrThrow
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstruction
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.morphe.util.indexOfFirstStringInstruction
import app.morphe.util.indexOfFirstStringInstructionOrThrow
import app.morphe.util.or
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "$PATCHES_PATH/GeneralAdsPatch;"

@Suppress("unused")
val adsPatch = bytecodePatch(
    HIDE_ADS.title,
    HIDE_ADS.summary,
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(settingsPatch)

    execute {
        // region Filter promoted ads (does not work in popular or latest feed)
        listOf(
            listingFingerprint,
            submittedListingFingerprint
        ).forEach { fingerprint ->
            fingerprint.methodOrThrow().apply {
                val targetIndex = indexOfFirstInstructionOrThrow {
                    getReference<FieldReference>()?.name == "children"
                }
                val targetRegister = getInstruction<TwoRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex, """
                    invoke-static {v$targetRegister}, $EXTENSION_CLASS_DESCRIPTOR->hideOldPostAds(Ljava/util/List;)Ljava/util/List;
                    move-result-object v$targetRegister
                    """
                )
            }
        }

        if (!is_2025_40_or_greater) {
            // The new feeds work by inserting posts into lists.
            // AdElementConverter is conveniently responsible for inserting all feed ads.
            // By removing the appending instruction no ad posts gets appended to the feed.
            val newAdPostMethod = newAdPostFingerprint.second.methodOrNull
                ?: newAdPostLegacyFingerprint.methodOrThrow()

            newAdPostMethod.apply {
                val startIndex =
                    0.coerceAtLeast(indexOfFirstStringInstruction("android_feed_freeform_render_variant"))
                val targetIndex = indexOfAddArrayListInstruction(this, startIndex)
                val targetInstruction = getInstruction<FiveRegisterInstruction>(targetIndex)

                replaceInstruction(
                    targetIndex,
                    "invoke-static {v${targetInstruction.registerC}, v${targetInstruction.registerD}}, " +
                            "$EXTENSION_CLASS_DESCRIPTOR->hideNewPostAds(Ljava/util/ArrayList;Ljava/lang/Object;)V"
                )
            }
        } else {
            val immutableListBuilderReference =
                with (immutableListBuilderFingerprint.methodOrThrow()) {
                    val index = indexOfImmutableListBuilderInstruction(this)

                    getInstruction<ReferenceInstruction>(index).reference
                }

            // TODO: Check this
            adPostSectionConstructorFingerprint.second.also {
                this.mutableClassDefBy(adPostSectionToStringFingerprint.mutableClassOrThrow())
            }.method.apply {
                val sectionIndex =
                    indexOfFirstStringInstructionOrThrow("sections")
                val sectionRegister =
                    getInstruction<FiveRegisterInstruction>(sectionIndex + 1).registerC

                addInstructions(
                    sectionIndex, """
                        invoke-static {v$sectionRegister}, $EXTENSION_CLASS_DESCRIPTOR->hideNewPostAds(Ljava/util/List;)Ljava/util/List;
                        move-result-object v$sectionRegister
                        if-nez v$sectionRegister, :ignore
                        new-instance v$sectionRegister, Ljava/util/ArrayList;
                        invoke-direct {v$sectionRegister}, Ljava/util/ArrayList;-><init>()V
                        invoke-static {v$sectionRegister}, $immutableListBuilderReference
                        move-result-object v$sectionRegister
                        :ignore
                        nop
                        """
                )
            }
        }

        // region Filter comment ads
        fun MutableMethod.hook() =
            addInstructionsWithLabels(
                0, """
                    invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->hideCommentAds()Z
                    move-result v0
                    if-eqz v0, :show
                    return-void
                    :show
                    nop
                    """
            )
        if (!is_2025_06_or_greater) {
            val isCommentAdsMethod: Method.() -> Boolean = {
                parameterTypes.size == 1 &&
                        parameterTypes.first().startsWith("Lcom/reddit/ads/conversation/") &&
                        accessFlags == AccessFlags.PUBLIC or AccessFlags.FINAL &&
                        returnType == "V" &&
                        indexOfFirstStringInstruction("ad") >= 0
            }

            classDefForEach { classDef ->
                classDef.methods.forEach { method ->
                    if (method.isCommentAdsMethod()) {
                        mutableClassDefBy(classDef)
                            .findMutableMethodOf(method)
                            .hook()
                    }
                }
            }
        } else if (!is_2025_40_or_greater) {
            listOf(
                commentAdCommentScreenAdViewFingerprint,
                commentAdDetailListHeaderViewFingerprint,
                commentsViewModelFingerprint
            ).forEach { fingerprint ->
                fingerprint.methodOrThrow().hook()
            }
        } else {
            commentsViewModelConstructorFingerprint.second.classDef.let {
                it.methods.filter { method ->
                    method.indexOfFirstInstruction {
                        opcode == Opcode.INVOKE_DIRECT &&
                                getReference<MethodReference>()?.toString()
                                    ?.endsWith("<init>(ZI)V") == true
                    } >= 0
                }.forEach { method ->
                    it.findMutableMethodOf(method).hook()
                }
            }

            postDetailAdLoaderFingerprint.methodOrThrow().apply {
                implementation!!.instructions
                    .withIndex()
                    .filter { (_, instruction) ->
                        val reference =
                            (instruction as? ReferenceInstruction)?.reference
                        reference is MethodReference &&
                                reference.toString() == "Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
                    }
                    .map { (index, _) -> index }
                    .reversed()
                    .forEach { index ->
                        val instruction =
                            getInstruction<FiveRegisterInstruction>(index)

                        // TODO: Look at this later, because the return type of this call is weird (it can return either the map or the value put into the map).
                        replaceInstruction(
                            index,
                            "invoke-static { v${instruction.registerC}, v${instruction.registerD}, v${instruction.registerE} }, " +
                                    "$EXTENSION_CLASS_DESCRIPTOR->hideCommentAdMap(Ljava/util/Map;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
                        )
                    }
            }
        }

        updatePatchStatus(
            "enableGeneralAds",
            HIDE_ADS
        )
    }
}
