package com.zani.launcher.myapp

import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/** A version entry published by Mojang's official Java version manifest. */
data class MinecraftVersionEntry(
    val id: String,
    val type: String,
    val metadataUrl: String,
    val releaseTime: String,
    val sha1: String,
)

/**
 * Reads public version metadata from Mojang's official manifest.
 *
 * This intentionally downloads metadata only: it does not fetch game JARs, assets, libraries,
 * authenticate accounts, or bypass Minecraft ownership requirements. Call from a worker thread;
 * the network request is synchronous and must not run on Android's main thread.
 */
object OfficialMinecraftCatalog {
    private const val MANIFEST_URL =
        "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    private const val MAX_MANIFEST_BYTES = 4 * 1024 * 1024
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 15_000

    /** Fetches release versions; set [includeSnapshots] to include non-release entries too. */
    @Throws(IOException::class)
    fun fetchVersions(includeSnapshots: Boolean = false): List<MinecraftVersionEntry> {
        val connection = (URL(MANIFEST_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "ZaniLauncher/0.1 (Android)")
        }

        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Minecraft version manifest returned HTTP ${connection.responseCode}")
            }
            val bytes = connection.inputStream.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > MAX_MANIFEST_BYTES) {
                        throw IOException("Minecraft version manifest is unexpectedly large")
                    }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
            val versions = JSONObject(String(bytes, StandardCharsets.UTF_8)).getJSONArray("versions")
            return buildList {
                for (index in 0 until versions.length()) {
                    val version = versions.getJSONObject(index)
                    val type = version.optString("type")
                    if (!includeSnapshots && type != "release") continue
                    val metadataUrl = version.optString("url")
                    if (!metadataUrl.startsWith("https://")) continue
                    add(
                        MinecraftVersionEntry(
                            id = version.getString("id"),
                            type = type,
                            metadataUrl = metadataUrl,
                            releaseTime = version.optString("releaseTime"),
                            sha1 = version.optString("sha1"),
                        ),
                    )
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}
