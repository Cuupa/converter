package com.cuupa.converter.services

import com.cuupa.converter.services.command.FileFormat
import com.cuupa.converter.services.command.convert.ConvertCommand
import com.cuupa.converter.services.command.convert.EMLToPdfCommand
import com.cuupa.converter.services.command.convert.WordToPdfCommand
import com.cuupa.converter.to.Document

class ConvertCommandFactory {

    companion object {
        private val commands = listOf(WordToPdfCommand(), EMLToPdfCommand())

        @JvmStatic
        fun getCommand(document: Document, fileFormat: FileFormat): ConvertCommand? {
            return commands.stream().filter { it.isApplicable(document, fileFormat) }.findFirst().orElseThrow()
        }
    }
}
