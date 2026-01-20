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

interface DataRepository {
    fun getAssetsList(): List<AssetInfo>
}

@Singleton
class DataRepositoryImpl @Inject constructor() : DataRepository {
    val fileSystem = FileSystem.SYSTEM

    override fun getAssetsList(): List<AssetInfo> =
        getBlueprintsList() + getWorldsList() + getCustomSolarSystemsPath() + getCustomTranslationsList()

    private fun getBlueprintsList(): List<AssetInfo> {
        val directory = SfsFileConfig.blueprintsPath
        val filesSequence = fileSystem.list(directory)
        return filesSequence
            .filter { path ->
                val metadata = fileSystem.metadata(path)
                metadata.isDirectory
            }
            .map { path ->
                AssetInfo(
                    name = path.name,
                    type = AssetType.Blueprint
                )
            }
            .toList()
    }

    private fun getWorldsList(): List<AssetInfo> {
        val directory = SfsFileConfig.worldsPath
        val filesSequence = fileSystem.list(directory)
        return filesSequence
            .filter { path ->
                val metadata = fileSystem.metadata(path)
                metadata.isDirectory
            }
            .map { path ->
                AssetInfo(
                    name = path.name,
                    type = AssetType.World
                )
            }
            .toList()
    }

    private fun getCustomSolarSystemsPath(): List<AssetInfo> {
        val example = "Example"
        val directory = SfsFileConfig.customSolarSystemsPath
        val filesSequence = fileSystem.list(directory)
        return filesSequence
            .filter { path ->
                val metadata = fileSystem.metadata(path)
                metadata.isDirectory &&
                path.name != example
            }
            .map { path ->
                AssetInfo(
                    name = path.name,
                    type = AssetType.CustomSolarSystem
                )
            }
            .toList()
    }

    private fun getCustomTranslationsList(): List<AssetInfo> {
        val exampleFile = "Example.txt"
        val directory = SfsFileConfig.customTranslationsPath
        val filesSequence = fileSystem.list(directory)
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
                    type = AssetType.CustomTranslation
                )
            }
            .toList()
    }
}