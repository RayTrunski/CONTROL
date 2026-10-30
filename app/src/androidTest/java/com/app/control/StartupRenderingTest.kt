package com.app.control

import android.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StartupRenderingTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun startupDrawsRealPixelsAndNavigationRemainsUsable() {
        compose.waitUntil(timeoutMillis = 20000) { compose.onAllNodesWithText("Saldo total").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Saldo total").assertIsDisplayed()
        // Semantics can exist while SurfaceFlinger still shows a black screen.
        // Validate the actual display, rather than only the Compose node tree.
        compose.waitUntil(timeoutMillis = 20000) {
            val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            if(bitmap == null) false else try {
                var green = 0
                var light = 0
                var samples = 0
                for(y in 0 until bitmap.height step 12) for(x in 0 until bitmap.width step 12) {
                    val pixel = bitmap.getPixel(x,y)
                    if(Color.green(pixel)>75 && Color.green(pixel)>Color.red(pixel)*1.4 && Color.green(pixel)>Color.blue(pixel)*1.15) green++
                    if(Color.red(pixel)>220 && Color.green(pixel)>220 && Color.blue(pixel)>220) light++
                    samples++
                }
                green.toDouble()/samples > .08 && light.toDouble()/samples > .30
            } finally { bitmap.recycle() }
        }
        compose.onNode(hasText("Movimientos") and hasClickAction()).performClick()
        compose.onNodeWithText("Buscar movimientos…").assertIsDisplayed()
        compose.onNode(hasText("Apartados") and hasClickAction()).performClick()
        compose.onNodeWithText("Total en apartados").assertIsDisplayed()
        compose.onNodeWithContentDescription("Abrir ajustes y perfil").performClick()
        compose.onNodeWithText("Ajustes y perfil").assertIsDisplayed()
        compose.onNodeWithText("‹ Volver").performClick()
        compose.onNodeWithText("Total en apartados").assertIsDisplayed()
    }
}
