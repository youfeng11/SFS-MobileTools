package com.youfeng.sfs.mobiletools.util

import okio.FileSystem
import okio.Path

fun Path.sizeInKb(): Double {
    val fs = FileSystem.SYSTEM
    val metadata = fs.metadata(this)

    val bytes = if (metadata.isDirectory) {
        fs.listRecursively(this)
            .filter { !fs.metadata(it).isDirectory }
            .sumOf { fs.metadata(it).size ?: 0L }
    } else {
        metadata.size ?: 0L
    }

    return bytes / 1024.0
}