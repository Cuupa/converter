package com.cuupa.converter.services.command

import com.cuupa.converter.services.command.WordDocument.Companion.loadDocument
import com.cuupa.converter.to.Document

class WordCommand(private val document: Document) : Command() {

    override fun execute(): Document {
        loadDocument(document).use { doc -> document.content = doc.removeMakros() }
        return document
    }
}