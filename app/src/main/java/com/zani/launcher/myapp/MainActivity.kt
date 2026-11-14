package com.zani.launcher.myapp

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.File

class MainActivity : Activity() {
    private val versions = listOf("1.21.1", "1.20.4", "1.19.4")
    private var selectedVersion = versions.first()
    private lateinit var versionLabel: TextView
    private lateinit var gameHomeLabel: TextView
    private var rustCoreReady = false
    private var gameHomeReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(16, 21, 29)
        window.navigationBarColor = Color.rgb(16, 21, 29)
        val nativeReady = runCatching { System.loadLibrary("zani_native"); true }.getOrDefault(false)
        if (nativeReady) {
            rustCoreReady = runCatching { nativeRustAvailable() }.getOrDefault(false)
            val result = runCatching { nativePrepareGameHome(File(filesDir, "minecraft").absolutePath) }
                .getOrElse { "Could not initialize Minecraft folder: ${it.message ?: "native error"}" }
            gameHomeReady = result == "Minecraft folder structure is ready."
        }
        setContentView(buildScreen(nativeReady))
    }

    private fun buildScreen(nativeReady: Boolean): ScrollView {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(28))
            setBackgroundColor(Color.rgb(16, 21, 29))
        }
        page.addView(label("ZANI  /  JAVA EDITION", 13, 0xFF70E0B5.toInt(), true))
        page.addView(label("Your worlds\nstart here.", 32, Color.WHITE, true).apply {
            setPadding(0, dp(8), 0, dp(6))
        })
        page.addView(label("A native-ready Minecraft launcher for Android.", 15, 0xFFAFBAC8.toInt(), false))

        val hero = card()
        hero.addView(label("GAME PROFILE", 12, 0xFF70E0B5.toInt(), true))
        hero.addView(label("Choose a version", 20, Color.WHITE, true).apply {
            setPadding(0, dp(12), 0, dp(12))
        })
        versionLabel = label("Minecraft $selectedVersion  ▾", 16, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            background = rounded(0xFF293444.toInt(), 14)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setOnClickListener { chooseVersion() }
        }
        hero.addView(versionLabel, matchWrap())
        val play = Button(this).apply {
            text = "PLAY"
            isAllCaps = true
            setTextColor(0xFF10221D.toInt())
            setBackgroundColor(0xFF70E0B5.toInt())
            setOnClickListener { explainLaunchStatus() }
        }
        hero.addView(play, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply { topMargin = dp(14) })
        hero.addView(label("Game installation and Java runtime are not set up yet.", 13, 0xFFFFD18B.toInt(), false).apply {
            setPadding(0, dp(10), 0, 0)
        })
        page.addView(hero, sectionParams())

        val files = card()
        files.addView(label("MINECRAFT FILES", 12, 0xFF70E0B5.toInt(), true))
        gameHomeLabel = label("Preparing private game directory…", 14, 0xFFAFBAC8.toInt(), false).apply {
            setPadding(0, dp(10), 0, dp(8))
        }
        files.addView(gameHomeLabel)
        files.addView(label("Workspace includes versions/, assets/, libraries/, runtime/, logs/, mods/ and config/. The folders are empty until you install authorized game files.", 13, 0xFFAFBAC8.toInt(), false))
        page.addView(files, sectionParams())

        val status = card()
        status.addView(label("NATIVE STATUS", 12, 0xFF70E0B5.toInt(), true))
        status.addView(label(if (nativeReady) nativeStatus() else "Native library not loaded; build with Android Studio + NDK.", 14, Color.WHITE, false).apply {
            setPadding(0, dp(10), 0, dp(4))
        })
        status.addView(label(if (rustCoreReady) "Rust core linked for this device ABI." else "Rust core not linked for this ABI; the C fallback remains active.", 13, 0xFFAFBAC8.toInt(), false))
        page.addView(status, sectionParams())

        val about = card()
        about.addView(label("NEXT: AUTHENTICATED INSTALL", 12, 0xFF70E0B5.toInt(), true))
        about.addView(label("A real game launch also needs Microsoft sign-in, version metadata, verified asset/library downloads, a compatible Android Java runtime, native graphics integration, and correct JVM arguments.", 14, Color.WHITE, false).apply {
            setPadding(0, dp(10), 0, dp(6))
        })
        about.addView(label("Zalith Launcher 2 is a source of high-level inspiration. This app does not include its code or Minecraft files.", 13, 0xFFAFBAC8.toInt(), false))
        page.addView(about, sectionParams())

        gameHomeLabel.text = if (gameHomeReady) "Ready: ${File(filesDir, "minecraft").absolutePath}" else "Unavailable: native game directory could not be prepared."
        return ScrollView(this).apply { isFillViewport = true; addView(page) }
    }

    private fun explainLaunchStatus() {
        val jar = File(filesDir, "minecraft/versions/$selectedVersion/$selectedVersion.jar")
        val message = when {
            !gameHomeReady -> "Minecraft folder is not ready. Check native status."
            !nativeValidateVersion(selectedVersion) -> "Invalid Minecraft version identifier."
            !jar.isFile -> "Minecraft $selectedVersion is not installed. No game JAR is bundled; install game files through an authorized account before launching."
            else -> "Game files exist, but authenticated version metadata, a compatible Android Java runtime, native libraries and launch arguments are still required."
        }
        AlertDialog.Builder(this).setTitle("Launch not ready").setMessage(message).setPositiveButton("OK", null).show()
    }

    private fun chooseVersion() {
        AlertDialog.Builder(this).setTitle("Select version")
            .setItems(versions.toTypedArray()) { _, which ->
                selectedVersion = versions[which]
                versionLabel.text = "Minecraft $selectedVersion  ▾"
            }.setNegativeButton("Cancel", null).show()
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(18), dp(18), dp(18), dp(18))
        background = rounded(0xFF1B2430.toInt(), 18)
    }

    private fun label(text: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        this.text = text
        textSize = size.toFloat()
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun sectionParams() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(18) }
    private fun matchWrap() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private external fun nativeStatus(): String
    private external fun nativeRustAvailable(): Boolean
    private external fun nativeValidateVersion(version: String): Boolean
    private external fun nativePrepareGameHome(path: String): String
}
