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

class MainActivity : Activity() {
    private val versions = listOf("1.21.1", "1.20.4", "1.19.4")
    private var selectedVersion = versions.first()
    private lateinit var versionLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(16, 21, 29)
        window.navigationBarColor = Color.rgb(16, 21, 29)
        setContentView(buildScreen())
    }

    private fun buildScreen(): ScrollView {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(28))
            setBackgroundColor(Color.rgb(16, 21, 29))
        }
        page.addView(label("ZANI", 14, Color.rgb(112, 224, 181), true))
        page.addView(label("Minecraft, your way.", 30, Color.WHITE, true).apply {
            setPadding(0, dp(8), 0, dp(6))
        })
        page.addView(label("A community-built Android Java launcher foundation.", 15, 0xFFAFBAC8.toInt(), false))

        val hero = card()
        hero.addView(label("READY WHEN YOU ARE", 12, 0xFF70E0B5.toInt(), true))
        hero.addView(label("Choose a game version", 20, Color.WHITE, true).apply {
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
            setOnClickListener {
                Toast.makeText(this@MainActivity, "Game runtime setup is not implemented yet. See README for the project roadmap.", Toast.LENGTH_LONG).show()
            }
        }
        val playParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply { topMargin = dp(14) }
        hero.addView(play, playParams)
        page.addView(hero, sectionParams())

        val status = card()
        status.addView(label("LAUNCHER STATUS", 12, 0xFF70E0B5.toInt(), true))
        status.addView(label("Native core", 17, Color.WHITE, true).apply { setPadding(0, dp(12), 0, dp(4)) })
        val nativeMessage = runCatching {
            System.loadLibrary("zani_native")
            nativeStatus()
        }.getOrElse { "Native library not loaded (open with Android Studio + NDK)." }
        status.addView(label(nativeMessage, 14, 0xFFAFBAC8.toInt(), false))
        status.addView(label("Account sign-in, downloads, and game execution are not wired up yet.", 14, 0xFFFFD18B.toInt(), false).apply {
            setPadding(0, dp(14), 0, 0)
        })
        page.addView(status, sectionParams())

        val about = card()
        about.addView(label("BUILT FOR ANDROID", 12, 0xFF70E0B5.toInt(), true))
        about.addView(label("Kotlin app · C/C++ JNI · Rust core", 17, Color.WHITE, true).apply {
            setPadding(0, dp(10), 0, dp(6))
        })
        about.addView(label("This starter project is not affiliated with Mojang or Microsoft. You must provide a legally obtained Minecraft account and game files.", 14, 0xFFAFBAC8.toInt(), false))
        page.addView(about, sectionParams())

        return ScrollView(this).apply {
            isFillViewport = true
            addView(page)
        }
    }

    private fun chooseVersion() {
        AlertDialog.Builder(this)
            .setTitle("Select version")
            .setItems(versions.toTypedArray()) { _, which ->
                selectedVersion = versions[which]
                versionLabel.text = "Minecraft $selectedVersion  ▾"
            }
            .setNegativeButton("Cancel", null)
            .show()
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

    private fun sectionParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = dp(18) }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private external fun nativeStatus(): String
}
