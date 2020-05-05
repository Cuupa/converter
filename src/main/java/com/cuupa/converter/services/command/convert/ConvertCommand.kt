package com.cuupa.converter.services.command.convert

import com.cuupa.converter.services.command.FileFormat
import com.cuupa.converter.services.command.clean.Command
import com.cuupa.converter.to.Document

abstract class ConvertCommand : Command() {

    abstract fun isApplicable(document: Document, fileFormat: FileFormat): Boolean
}
