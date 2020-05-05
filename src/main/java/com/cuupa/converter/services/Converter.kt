package com.cuupa.converter.services

import com.cuupa.converter.services.command.CleanCommandFactory
import com.cuupa.converter.services.command.CommandExecuter
import com.cuupa.converter.services.command.FileFormat
import com.cuupa.converter.to.Document

class Converter {

    fun convert(document: Document): Document? {
        return CommandExecuter.execute(CleanCommandFactory.getCommand(document), document)
    }

    fun convertTo(document: Document, fileFormat: FileFormat): Document? {
        val cleanedDoc = CommandExecuter.execute(CleanCommandFactory.getCommand(document), document)
        return if (cleanedDoc != null) {
            CommandExecuter.execute(ConvertCommandFactory.getCommand(cleanedDoc, fileFormat), cleanedDoc, fileFormat)
        } else {
            null
        }
    }
}