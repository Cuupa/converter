package com.cuupa.converter.services

import com.cuupa.converter.services.command.*
import com.cuupa.converter.to.Document

class Converter {

    fun convert(document: Document): Document? {
        var command: Command? = null
        if (isPdf(document)) {
            command = PDFCommand(document)
        } else if (isEML(document)) {
            command = EMLCommand(document)
        } else if (isWord(document)) {
            command = WordCommand(document)
        }
        return CommandExecuter.execute(command)
    }

    private fun isWord(document: Document): Boolean {
        return getFileEnding(document.filename) == "docx" || getFileEnding(document.filename) == "docm"
    }

    private fun isEML(document: Document): Boolean {
        return getFileEnding(document.filename).equals("eml", ignoreCase = true)
    }

    private fun isPdf(document: Document): Boolean {
        return getFileEnding(document.filename).equals("pdf", ignoreCase = true)
    }

    private fun getFileEnding(name: String): String {
        val split = name.split(Regex("\\.")).toTypedArray()
        return split[split.size - 1]
    }
}