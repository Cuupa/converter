package com.cuupa.converter.services.command.clean

import com.cuupa.converter.services.command.FileFormat
import com.cuupa.converter.services.command.WordDocument.Companion.loadDocument
import com.cuupa.converter.to.Document

class WordCommand : CleanCommand() {

    override fun isApplicable(document: Document): Boolean {
        return getFileEnding(document.filename) == "docx" || getFileEnding(document.filename) == "docm"
    }

    override fun execute(document: Document, fileFormat: FileFormat): Document {
        loadDocument(document).use { doc -> document.content = doc.removeMakros() }
        return document
    }
}