package com.youfeng.sfs.mobiletools.data.repository

import android.content.Context
import com.youfeng.sfs.mobiletools.common.model.AssetInfo
import com.youfeng.sfs.mobiletools.common.model.AssetType
import com.youfeng.sfs.mobiletools.data.SfsFileConfig
import javax.inject.Inject
import javax.inject.Singleton
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path
import timber.log.Timber
import com.youfeng.sfs.mobiletools.common.model.ModType

interface DataRepository {
    fun getAssetsList(): List<AssetInfo>
    fun deleteAsset(asset: AssetInfo)
}

@Singleton
class DataRepositoryImpl @Inject constructor() : DataRepository {
    val fileSystem = FileSystem.SYSTEM

    override fun getAssetsList(): List<AssetInfo> =
        getBlueprintsList() + getModsList() + getWorldsList() + getCustomSolarSystemsPath() + getCustomTranslationsList()

    private fun getBlueprintsList(): List<AssetInfo> {
        val directory = SfsFileConfig.blueprintsPath
        val filesSequence = fileSystem.listOrNull(directory) ?: emptyList()
        Timber.v(filesSequence.toString())
        return filesSequence
            .filter { path ->
                val metadata = fileSystem.metadata(path)
                metadata.isDirectory
            }
            .map { path ->
                AssetInfo(
                    name = path.name,
                    type = AssetType.Blueprint,
                    size = path.sizeInKb()
                )
            }
            .toList()
    }

    private fun getModsList(): List<AssetInfo> {
        val partsFilesSequence = fileSystem.listOrNull(SfsFileConfig.partsModsPath) ?: emptyList()
        Timber.v(partsFilesSequence.toString())
        val partsList = partsFilesSequence
            .filter { path ->
                val metadata = fileSystem.metadata(path)
                !metadata.isDirectory &&
                path.name.endsWith(".pack")
            }
            .map { path ->
                AssetInfo(
                    name = path.name.removeSuffix(".pack"),
                    type = AssetType.Mod(ModType.PART_ASSET_PACK),
                    size = path.sizeInKb()
                )
            }
        val example = "Example"
        val texturePacksFilesSequence = fileSystem.listOrNull(SfsFileConfig.texturePacksModsPath) ?: emptyList()
        Timber.v(texturePacksFilesSequence.toString())
        val texturePacksList = texturePacksFilesSequence
            .filter { path ->
                val metadata = fileSystem.metadata(path)
                metadata.isDirectory &&
                path.name != example
            }
            .map { path ->
                AssetInfo(
                    name = path.name,
                    type = AssetType.Mod(ModType.TEXTURE_PACK),
                    size = path.sizeInKb()
                )
            }
        return partsList + texturePacksList
    }

    private fun getWorldsList(): List<AssetInfo> {
        val directory = SfsFileConfig.worldsPath
        val filesSequence = fileSystem.listOrNull(directory) ?: emptyList()
        Timber.v(filesSequence.toString())
        return filesSequence
            .filter { path ->
                val metadata = fileSystem.metadata(path)
                metadata.isDirectory
            }
            .map { path ->
                AssetInfo(
                    name = path.name,
                    type = AssetType.World,
                    size = path.sizeInKb()
                )
            }
            .toList()
    }

    private fun getCustomSolarSystemsPath(): List<AssetInfo> {
        val example = "Example"
        val directory = SfsFileConfig.customSolarSystemsPath
        val filesSequence = fileSystem.listOrNull(directory) ?: emptyList()
        Timber.v(filesSequence.toString())
        return filesSequence
            .filter { path ->
                val metadata = fileSystem.metadata(path)
                metadata.isDirectory &&
                path.name != example
            }
            .map { path ->
                AssetInfo(
                    name = path.name,
                    type = AssetType.CustomSolarSystem,
                    size = path.sizeInKb()
                )
            }
            .toList()
    }

    private fun getCustomTranslationsList(): List<AssetInfo> {
        val exampleFile = "Example.txt"
        val directory = SfsFileConfig.customTranslationsPath
        val filesSequence = fileSystem.listOrNull(directory) ?: emptyList()
        Timber.v(filesSequence.toString())
        return filesSequence
            .filter { path ->
                val metadata = fileSystem.metadata(path)
                !metadata.isDirectory &&
                path.name.endsWith(".txt") &&
                path.name != exampleFile
            }
            .map { path ->
                AssetInfo(
                    name = path.name.removeSuffix(".txt"),
                    type = AssetType.CustomTranslation,
                    size = path.sizeInKb()
                )
            }
            .toList()
    }

    override fun deleteAsset(asset: AssetInfo) {
        val path: Path? = when (val type = asset.type) {
            is AssetType.Blueprint -> SfsFileConfig.blueprintsPath / asset.name
            is AssetType.Mod -> {
                when (type.type) {
                    ModType.PART_ASSET_PACK -> SfsFileConfig.partsModsPath / "${asset.name}.pack"
                    ModType.TEXTURE_PACK -> SfsFileConfig.texturePacksModsPath / asset.name
                    ModType.CODE_MOD -> null // 如果有代码插件路径，请补充
                }
            }
            is AssetType.World -> SfsFileConfig.worldsPath / asset.name
            is AssetType.CustomSolarSystem -> SfsFileConfig.customSolarSystemsPath / asset.name
            is AssetType.CustomTranslation -> SfsFileConfig.customTranslationsPath / "${asset.name}.txt"
        }

        path?.let {
            // 使用 deleteRecursively 以确保可以删除文件夹及其内容
            fileSystem.deleteRecursively(it)
        }
    }

    private fun Path.sizeInKb(): Double {
    val metadata = fileSystem.metadata(this)

    val bytes = if (metadata.isDirectory) {
        fileSystem.listRecursively(this)
            .filter { !fileSystem.metadata(it).isDirectory }
            .sumOf { fileSystem.metadata(it).size ?: 0L }
    } else {
        metadata.size ?: 0L
    }

    return bytes / 1024.0
}
}
