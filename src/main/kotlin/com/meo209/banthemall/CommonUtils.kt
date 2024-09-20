package com.meo209.banthemall

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest

object CommonUtils {

    fun generateModData(directory: File): List<String> =
        directory.listFiles()
            ?.filter { it.isFile && it.extension == "jar" }
            ?.map { "${it.name}:${sha256Hash(it)}" }
            ?: emptyList()

    fun sha256Hash(string: String): String {
        val bytes = string.toByteArray()
        val md = MessageDigest.getInstance("SHA256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun sha256Hash(file: File): String {
        val bytes = Files.readAllBytes(Paths.get(file.absolutePath))
        val md = MessageDigest.getInstance("SHA256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

}