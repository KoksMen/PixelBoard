package com.akshaykadam.pixelboard.patches.gboard.features.spacebar

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertTrue
import org.junit.Test

class GboardSpacebarSwipePatchSourceTest {
    @Test
    fun `spacebar swipe patch targets SoftKeyView classes and emits runtime calls`() {
        val source = Files.readString(
            repositoryRoot().resolve(
                "patches/src/main/kotlin/com/akshaykadam/pixelboard/patches/gboard/" +
                    "features/spacebar/GboardSpacebarSwipePatch.kt",
            ),
            StandardCharsets.UTF_8,
        )

        assertTrue(source.contains("SoftKeyView"))
        assertTrue(source.contains("onAttachedToWindow"))
        assertTrue(source.contains("dispatchTouchEvent"))
        assertTrue(source.contains("onTouchEvent"))
        assertTrue(source.contains("SPACEBAR_SWIPE_RUNTIME_HANDLE_TOUCH_EVENT"))
        assertTrue(source.contains("SPACEBAR_SWIPE_RUNTIME_ON_ATTACHED"))
        assertTrue(source.contains("RuntimeCallEmitter.invoke(attachedCall, \"p0\")"))
        assertTrue(source.contains("RuntimeCallEmitter.invoke(touchCall, \"p0, p1\")"))
    }

    private fun repositoryRoot(): Path {
        val workingDirectory = Path.of("").toAbsolutePath().normalize()
        return generateSequence(workingDirectory) { it.parent }
            .firstOrNull { Files.isRegularFile(it.resolve("settings.gradle.kts")) }
            ?: error("Could not locate repository root from $workingDirectory")
    }
}
