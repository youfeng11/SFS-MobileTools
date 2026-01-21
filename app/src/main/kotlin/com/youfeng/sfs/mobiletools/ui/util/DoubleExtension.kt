package com.youfeng.sfs.mobiletools.ui.util

fun Double.formatSizeFromKB(): String {
    if (this < 1024) {
        return "%.1fKB".format(this)
    }
    val mb = this / 1024
    if (mb < 1024) {
        return "%.1fMB".format(mb)
    }
    val gb = mb / 1024
    return "%.2fGB".format(gb)
}