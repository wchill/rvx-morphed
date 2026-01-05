package app.morphe.patches.reddit.layout.sidebar

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.utils.extension.Constants.PATCHES_PATH
import app.morphe.patches.reddit.utils.patch.PatchList.HIDE_SIDEBAR_COMPONENTS
import app.morphe.patches.reddit.utils.settings.is_2025_40_or_greater
import app.morphe.patches.reddit.utils.settings.updatePatchStatus
import app.morphe.util.fingerprint.methodOrThrow
import app.morphe.util.fingerprint.mutableClassOrThrow
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "$PATCHES_PATH/SidebarComponentsPatch;"

val sidebarComponentsPatch = bytecodePatch(
    HIDE_SIDEBAR_COMPONENTS.title,
    HIDE_SIDEBAR_COMPONENTS.summary,
) {
    execute {

        val communityDrawerPresenterConstructorMethod =
            communityDrawerPresenterConstructorFingerprint.methodOrThrow()

        // TODO: Check this
        val communityDrawerPresenterMethod =
            communityDrawerPresenterFingerprint.second.also {
                this.mutableClassDefBy(communityDrawerPresenterConstructorFingerprint.mutableClassOrThrow())
            }.method

        fun getDrawerField(
            fieldName: String,
            isRedditPro: Boolean
        ): FieldReference {
            val targetMethod = if (isRedditPro) {
                redditProLoaderFingerprint.methodOrThrow()
            } else {
                communityDrawerPresenterConstructorMethod
            }

            targetMethod.apply {
                val headerItemIndex =
                    indexOfHeaderItemInstruction(this, fieldName)
                val fieldIndex =
                    indexOfFirstInstructionOrThrow(headerItemIndex, Opcode.IPUT_OBJECT)

                return getInstruction<ReferenceInstruction>(fieldIndex).reference
                        as FieldReference
            }
        }

        fun hideShelf(
            fieldName: String,
            methodNamePrefix: String,
            isRedditPro: Boolean
        ) {
            val fieldReference = getDrawerField(fieldName, isRedditPro)

            communityDrawerPresenterMethod.apply {
                val fieldIndex =
                    indexOfFirstInstructionOrThrow {
                        opcode == Opcode.IGET_OBJECT &&
                                getReference<FieldReference>() == fieldReference
                    }

                val collectionIndex =
                    indexOfKotlinCollectionInstruction(this, fieldIndex)
                val collectionInstruction =
                    getInstruction<FiveRegisterInstruction>(collectionIndex)

                val iterableRegister = collectionInstruction.registerC
                val collectionRegister = collectionInstruction.registerD

                addInstructions(
                    collectionIndex, """
                        invoke-static {v$iterableRegister}, $EXTENSION_CLASS_DESCRIPTOR->${methodNamePrefix}Divider(Ljava/lang/Iterable;)Ljava/lang/Iterable;
                        move-result-object v$iterableRegister
                        invoke-static {v$collectionRegister}, $EXTENSION_CLASS_DESCRIPTOR->${methodNamePrefix}Shelf(Ljava/util/Collection;)Ljava/util/Collection;
                        move-result-object v$collectionRegister
                        """
                )
            }
        }

        val hooks = mutableListOf(
            Triple(
                "RECENTLY_VISITED",
                "hideRecentlyVisited",
                false
            )
        )

        if (is_2025_40_or_greater) {
            hooks += Triple(
                "GAMES_ON_REDDIT",
                "hideGamesOnReddit",
                false
            )
            hooks += Triple(
                "REDDIT_PRO",
                "hideRedditPro",
                true
            )
        }

        hooks.forEach { (fieldName, methodNamePrefix, isRedditPro) ->
            hideShelf(fieldName, methodNamePrefix, isRedditPro)
        }

        updatePatchStatus(
            "enableRecentlyVisitedShelf",
            HIDE_SIDEBAR_COMPONENTS
        )

        if (is_2025_40_or_greater) {
            updatePatchStatus(
                "enableGamesOnRedditShelf"
            )
            updatePatchStatus(
                "enableRedditProShelf"
            )
        }
    }
}