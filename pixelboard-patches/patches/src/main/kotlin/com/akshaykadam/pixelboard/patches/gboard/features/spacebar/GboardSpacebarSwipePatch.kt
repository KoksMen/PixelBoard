/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.patches.gboard.features.spacebar

import com.akshaykadam.pixelboard.patches.gboard.shared.GboardMethodTarget
import com.akshaykadam.pixelboard.patches.gboard.shared.findMutableMethodOrNull
import com.akshaykadam.pixelboard.patches.gboard.shared.gboardPatchesExtensionCarrierPatch
import com.akshaykadam.pixelboard.patches.gboard.shared.isMethodReference
import com.akshaykadam.pixelboard.patches.gboard.shared.runtimeabi.RuntimeAbiCatalog
import com.akshaykadam.pixelboard.patches.gboard.shared.runtimeabi.RuntimeCallEmitter
import com.akshaykadam.pixelboard.patches.gboard.shared.runtimeabi.RuntimeCallId
import com.akshaykadam.pixelboard.patches.shared.Constants.COMPATIBILITY_GBOARD
import com.akshaykadam.pixelboard.patches.shared.addInstructions
import com.akshaykadam.pixelboard.patches.shared.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode

private val softKeyViewAttachedTargets = listOf(
    GboardMethodTarget(
        classType = "Lcom/google/android/libraries/inputmethod/widgets/SoftKeyView;",
        name = "onAttachedToWindow",
        parameterTypes = emptyList(),
        returnType = "V",
    ),
)

private val softKeyViewTouchTargets = listOf(
    GboardMethodTarget(
        classType = "Lcom/google/android/libraries/inputmethod/widgets/SoftKeyView;",
        name = "dispatchTouchEvent",
        parameterTypes = listOf("Landroid/view/MotionEvent;"),
        returnType = "Z",
    ),
    GboardMethodTarget(
        classType = "Lcom/google/android/libraries/inputmethod/widgets/SoftKeyView;",
        name = "onTouchEvent",
        parameterTypes = listOf("Landroid/view/MotionEvent;"),
        returnType = "Z",
    ),
)

internal val gboardSpacebarSwipePatch = bytecodePatch(
    description = "Enable switching input languages by swiping left or right across the spacebar.",
) {
    compatibleWith(COMPATIBILITY_GBOARD)
    dependsOn(gboardPatchesExtensionCarrierPatch)

    execute {
        val attachedCall = RuntimeCallId.SPACEBAR_SWIPE_RUNTIME_ON_ATTACHED
        val attachedRef = RuntimeAbiCatalog.abi(attachedCall).reference
        softKeyViewAttachedTargets.forEach { target ->
            val method = findMutableMethodOrNull(target) ?: return@forEach
            val implementation = method.implementation ?: return@forEach
            val instructions = implementation.instructions
            if (instructions.any { it.isMethodReference(attachedRef) }) {
                return@forEach
            }
            val returnIndex = instructions.indexOfLast { it.opcode == Opcode.RETURN_VOID }
            if (returnIndex == -1) return@forEach
            method.addInstructions(
                returnIndex,
                RuntimeCallEmitter.invoke(attachedCall, "p0"),
            )
        }

        val touchCall = RuntimeCallId.SPACEBAR_SWIPE_RUNTIME_HANDLE_TOUCH_EVENT
        val touchRef = RuntimeAbiCatalog.abi(touchCall).reference
        softKeyViewTouchTargets.forEach { target ->
            val method = findMutableMethodOrNull(target) ?: return@forEach
            val implementation = method.implementation ?: return@forEach
            val instructions = implementation.instructions
            if (instructions.any { it.isMethodReference(touchRef) }) {
                return@forEach
            }
            method.addInstructions(
                0,
                """
                    ${RuntimeCallEmitter.invoke(touchCall, "p0, p1")}
                    move-result v0
                    if-eqz v0, :cond_spacebar_normal
                    const/4 v0, 0x1
                    return v0
                    :cond_spacebar_normal
                """.trimIndent(),
            )
        }
    }
}
