package com.cuupa.converter.to

import java.io.InputStream

class DocumentBuilder private constructor(private val inputStream: InputStream, private val filename: String,
                                          private val fileSize: Int) {
    fun build(): Document {
        val content = ByteArray(fileSize)
        inputStream.read(content)
        return Document(filename, content)
    }

    companion object {
        @JvmStatic
        fun create(inputStream: InputStream, fileName: String,
                   fileSize: Int): DocumentBuilder {
            return DocumentBuilder(inputStream, fileName, fileSize)
        }
    }

}