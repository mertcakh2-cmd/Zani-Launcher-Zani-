package com.zani.launcher.myapp

import android.content.Context
import java.io.File

/**
 * Real Minecraft game directory management for the launcher.
 *
 * The launcher uses the app-private directory:
 *   Context.getFilesDir()/minecraft
 *
 * That mirrors the repo's documented layout and keeps all downloaded runtime data
 * sandboxed away from the rest of the app.
 */
data class MinecraftGameDirectory(
    val root: File,
    val versions: File,
    val assets: File,
    val libraries: File,
    val runtime: File,
    val logs: File,
    val mods: File,
    val config: File,
) {
    fun ensureAll() {
        root.mkdirs()
        versions.mkdirs()
        assets.mkdirs()
        libraries.mkdirs()
        runtime.mkdirs()
        logs.mkdirs()
        mods.mkdirs()
        config.mkdirs()
    }
}

object RealMinecraft {
    const val GAME_DIR_NAME = "minecraft"

    fun getRoot(context: Context): File {
        return File(context.filesDir, GAME_DIR_NAME).apply {
            mkdirs()
        }
    }

    fun ensureDirectoryStructure(context: Context): MinecraftGameDirectory {
        val root = getRoot(context)
        val versions = File(root, "versions").apply { mkdirs() }
        val assets = File(root, "assets").apply { mkdirs() }
        val assetsIndexes = File(assets, "indexes").apply { mkdirs() }
        val assetsObjects = File(assets, "objects").apply { mkdirs() }
        val libraries = File(root, "libraries").apply { mkdirs() }
        val runtime = File(root, "runtime").apply { mkdirs() }
        val logs = File(root, "logs").apply { mkdirs() }
        val mods = File(root, "mods").apply { mkdirs() }
        val config = File(root, "config").apply { mkdirs() }

        return MinecraftGameDirectory(
            root = root,
            versions = versions,
            assets = assets,
            libraries = libraries,
            runtime = runtime,
            logs = logs,
            mods = mods,
            config = config,
        ).also {
            assetsIndexes.mkdirs()
            assetsObjects.mkdirs()
        }
    }

    fun isValidVersionId(versionId: String): Boolean {
        val trimmed = versionId.trim()
        if (trimmed.isEmpty()) return false
        return trimmed.matches(Regex("^[A-Za-z0-9_.-]+$"))
    }

    fun getVersionDirectory(context: Context, versionId: String): File {
        val safeVersionId = versionId.trim()
        require(isValidVersionId(safeVersionId)) { "Invalid Minecraft version id: $versionId" }

        val structure = ensureDirectoryStructure(context)
        return File(structure.versions, safeVersionId).apply { mkdirs() }
    }

    fun listVersionFolders(context: Context): List<String> {
        val versionsDir = File(getRoot(context), "versions")
        if (!versionsDir.exists() || !versionsDir.isDirectory) {
            return emptyList()
        }

        return versionsDir.listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
    }

    fun getAssetsDirectory(context: Context): File {
        return ensureDirectoryStructure(context).assets
    }

    fun getLibrariesDirectory(context: Context): File {
        return ensureDirectoryStructure(context).libraries
    }

    fun getRuntimeDirectory(context: Context): File {
        return ensureDirectoryStructure(context).runtime
    }

    fun getLogsDirectory(context: Context): File {
        return ensureDirectoryStructure(context).logs
    }

    fun getModsDirectory(context: Context): File {
        return ensureDirectoryStructure(context).mods
    }

    fun getConfigDirectory(context: Context): File {
        return ensureDirectoryStructure(context).config
    }

    fun createIfMissing(context: Context, relativePath: String, defaultContent: String = ""): File {
        val target = File(getRoot(context), relativePath)
        target.parentFile?.mkdirs()

        if (!target.exists()) {
            target.createNewFile()
            if (defaultContent.isNotEmpty()) {
                target.writeText(defaultContent)
            }
        }

        return target
    }
}
