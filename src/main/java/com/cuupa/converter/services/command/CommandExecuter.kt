package com.cuupa.converter.services.command

import com.cuupa.converter.services.command.clean.Command
import com.cuupa.converter.to.Document

object CommandExecuter {

    fun execute(command: Command?, document: Document): Document? {
        return execute(command, document, FileFormat.UNKNOWN)
    }

    fun execute(command: Command?, document: Document, fileFormat: FileFormat): Document? {
        return command?.execute(document, fileFormat)
    }
}