package com.youfeng.sfs.mobiletools.data.repository

import android.content.Context
import android.net.Uri
import com.youfeng.sfs.mobiletools.domain.model.AssetInfo
import com.youfeng.sfs.mobiletools.domain.model.AssetType
import com.youfeng.sfs.mobiletools.domain.model.ModType
import com.youfeng.sfs.mobiletools.data.SfsFileConfig
import com.youfeng.sfs.mobiletools.util.sizeInKb
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.FileSystem
import okio.Path
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

interface AssetsRepository {
    fun getAssetsList(): List<AssetInfo>
    fun deleteAsset(asset: AssetInfo)
    fun installAsset(assetType: AssetType, uri: Uri)
}

@Singleton
class AssetsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AssetsRepository {
    private val fileSystem = FileSystem.SYSTEM

    override fun getAssetsList(): List<AssetInfo> =
        getBlueprintsList() + getModsList() + getWorldsList() + getCustomSolarSystemsPath() + getCustomTranslationsList()

    private fun getBlueprintsList(): List<AssetInfo> {
        val directory = SfsFileConfig.blueprintsPath
        val filesSequence = fileSystem.listOrNull(directory) ?: return emptyList()
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
        val partsFilesSequence =
            fileSystem.listOrNull(SfsFileConfig.partsModsPath) ?: return emptyList()
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
        val texturePacksFilesSequence =
            fileSystem.listOrNull(SfsFileConfig.texturePacksModsPath) ?: return emptyList()
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
        val filesSequence = fileSystem.listOrNull(directory) ?: return emptyList()
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
        val filesSequence = fileSystem.listOrNull(directory) ?: return emptyList()
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
        val filesSequence = fileSystem.listOrNull(directory) ?: return emptyList()
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

    override fun installAsset(assetType: AssetType, uri: Uri) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("无法打开文件")

            // Get the file name from URI
            val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: "unknown"

            // Determine destination path based on asset type
            val destinationPath = when (assetType) {
                is AssetType.Blueprint -> {
                    // Blueprints are directories, need special handling
                    val dirName = fileName.substringBeforeLast(".")
                    SfsFileConfig.blueprintsPath / dirName
                }
                is AssetType.Mod -> {
                    when (assetType.type) {
                        ModType.PART_ASSET_PACK -> {
                            val packName = if (fileName.endsWith(".pack")) fileName else "$fileName.pack"
                            SfsFileConfig.partsModsPath / packName
                        }
                        ModType.TEXTURE_PACK -> {
                            SfsFileConfig.texturePacksModsPath / fileName
                        }
                        ModType.CODE_MOD -> throw UnsupportedOperationException("Code mods not yet supported")
                    }
                }
                is AssetType.World -> SfsFileConfig.worldsPath / fileName
                is AssetType.CustomSolarSystem -> SfsFileConfig.customSolarSystemsPath / fileName
                is AssetType.CustomTranslation -> {
                    val txtName = if (fileName.endsWith(".txt")) fileName else "$fileName.txt"
                    SfsFileConfig.customTranslationsPath / txtName
                }
            }

            // Create parent directories if they don't exist
            fileSystem.createDirectories(destinationPath.parent!!)

            // Copy file
            val outputFile = File(destinationPath.toString())
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            inputStream.close()
            Timber.i("Asset installed successfully at: $destinationPath")

        } catch (e: Exception) {
            Timber.e(e, "Failed to install asset")
            throw e
        }
    }
}
