package com.meo209.banthemall

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest

object CommonUtils {

    fun generateModIndex(directory: File): List<String> =
        directory.listFiles()
            ?.filter { it.isFile && it.extension == "jar" }
            ?.map { sha256Hash(it) }
            ?: emptyList()

    private fun sha256Hash(file: File): String {
        val bytes = Files.readAllBytes(Paths.get(file.absolutePath))
        val md = MessageDigest.getInstance("SHA256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

}