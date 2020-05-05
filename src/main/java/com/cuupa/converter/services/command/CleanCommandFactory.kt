package com.cuupa.converter.services.command

import com.cuupa.converter.services.command.clean.CleanCommand
import com.cuupa.converter.services.command.clean.PDFCommand
import com.cuupa.converter.services.command.clean.WordCommand
import com.cuupa.converter.to.Document

class CleanCommandFactory {

    companion object {
        private val commands = listOf(PDFCommand(), WordCommand())

        @JvmStatic
        fun getCommand(document: Document): CleanCommand? {
            return commands.stream().filter { it.isApplicable(document) }.findFirst().orElse(null)
        }
    }
}
