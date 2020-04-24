package com.cuupa.converter.services.command

import com.cuupa.converter.to.Document
import org.jdom2.Element
import org.jdom2.JDOMException
import org.jdom2.input.SAXBuilder
import org.jdom2.output.XMLOutputter
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class WordDocument private constructor(private val content: ByteArray) : AutoCloseable {
    private val dir = System.currentTimeMillis().toString()

    fun removeMakros(): ByteArray {
        extractZipToDisk(dir)
        removeFromContentTypes(dir)
        removeVBData(dir)
        val content = persist(dir)
        deleteTempFiles(dir)
        return content
    }

    private fun deleteTempFiles(dir: String) {
        deleteDir(dir)
        File(dir).delete()
    }

    private fun deleteDir(dir: String) {
        val files = File(dir).listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    deleteDir(file.absolutePath)
                    file.delete()
                } else {
                    file.delete()
                }
            }
        }
    }

    private fun persist(dir: String): ByteArray {
        val fileList: MutableList<String> = mutableListOf()
        generateFileList(File(dir), dir, fileList)
        var content = ByteArray(0)
        ByteArrayOutputStream().use { bos ->
            ZipOutputStream(bos).use { out ->
                for (_file in fileList) {
                    val buffer = ByteArray(1024)
                    val entry = ZipEntry(_file)
                    out.putNextEntry(entry)
                    FileInputStream(dir + File.separator + _file).use { `in` ->
                        var length: Int
                        while (`in`.read(buffer).also { length = it } > 0) {
                            out.write(buffer, 0, length)
                        }
                    }
                }
                content = bos.toByteArray()
                out.closeEntry()
            }
        }
        return content
    }

    private fun generateFileList(file: File, dir: String,
                                 value: MutableList<String>) {
        if (file.isFile) {
            value.add(generateZipEntry(file.toString(), dir))
        } else if (file.isDirectory) {
            val files = file.listFiles()
            if (files != null) {
                for (string in files) {
                    generateFileList(string, dir, value)
                }
            }
        }
    }

    private fun generateZipEntry(entry: String, dir: String): String {
        return entry.substring(entry.indexOf(File.separator) + 1, entry.length)
    }

    private fun removeVBData(dir: String) {
        val word = File(dir, "word")
        val listFiles = word.listFiles()
        for (file in listFiles) {
            if (file.name == "vbaData.xml" || file.name == "vbaProject.bin") {
                file.delete()
            }
        }
    }

    private fun removeFromContentTypes(dir: String) {
        val file = File(dir, CONTENT_TYPES_XML)
        val builder = SAXBuilder()
        try {
            val document = builder.build(file)
            val rootElement = document.rootElement
            val newChildren: MutableList<Element> = ArrayList()
            for (element in rootElement.children) {
                if (isAllowedChild(element)) {
                    newChildren.add(element)
                }
            }
            rootElement.setContent(newChildren)
            val xmlOutputter = XMLOutputter()
            FileOutputStream(file).use { out -> xmlOutputter.output(document, out) }
        } catch (e: JDOMException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    private fun isAllowedChild(element: Element): Boolean {
        return (APPLICATION_VND_MS_WORD_VBA_DATA_XML != element.getAttributeValue(CONTENT_TYPE)
                && WORD_VBA_DATA_XML != element.getAttributeValue(PART_NAME))
    }

    private fun extractZipToDisk(dir: String) {
        val buffer = ByteArray(1024)
        ZipInputStream(ByteArrayInputStream(content)).use { inputStream ->
            var zipEntry: ZipEntry? = null
            while (inputStream.nextEntry.also { zipEntry = it } != null) {
                val fileName = zipEntry!!.name
                val newFile = File(dir, fileName)
                newFile.parentFile.mkdirs()

                if (zipEntry!!.isDirectory) {
                    newFile.mkdir()
                    continue
                }
                FileOutputStream(newFile).use { fos ->
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } > 0) {
                        fos.write(buffer, 0, len)
                    }
                }
            }
        }
    }

    override fun close() {
        deleteTempFiles(dir)
    }

    companion object {
        private const val WORD_VBA_DATA_XML = "/word/vbaData.xml"
        private const val APPLICATION_VND_MS_WORD_VBA_DATA_XML = "application/vnd.ms-word.vbaData+xml"
        private const val PART_NAME = "PartName"
        private const val CONTENT_TYPE = "ContentType"
        private const val CONTENT_TYPES_XML = "[Content_Types].xml"

        @JvmStatic
        fun loadDocument(document: Document): WordDocument {
            return WordDocument(document.content)
        }
    }

}