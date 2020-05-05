package com.cuupa.converter.services.command.convert

import com.cuupa.converter.services.command.FileFormat
import com.cuupa.converter.to.Document
import com.google.common.io.Files
import org.apache.poi.xwpf.converter.pdf.PdfConverter
import org.apache.poi.xwpf.converter.pdf.PdfOptions
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class WordToPdfCommand : ConvertCommand() {

    override fun isApplicable(document: Document, fileFormat: FileFormat): Boolean {
        return (getFileEnding(document.filename) == "docx" || getFileEnding(document.filename) == "docm") &&
                fileFormat == FileFormat.PDF
    }

    override fun execute(document: Document, fileFormat: FileFormat): Document {
        val inputStream = saveFileInTemporaryFolder(document)
        val xwpfDocument = XWPFDocument(inputStream)
        val outputStream = ByteArrayOutputStream()
        PdfConverter.getInstance().convert(xwpfDocument, outputStream, PdfOptions.create())
        return Document(document.filename, outputStream.toByteArray(), document.encrypted)
    }

    private fun saveFileInTemporaryFolder(document: Document): FileInputStream {
        val tempDir = Files.createTempDir()
        val file = File(tempDir, document.filename)
        Files.write(document.content, file)
        return FileInputStream(file)
    }
}