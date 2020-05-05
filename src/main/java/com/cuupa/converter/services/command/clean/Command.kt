package com.cuupa.converter.services.command.clean

import com.cuupa.converter.services.command.FileFormat
import com.cuupa.converter.to.Document

abstract class Command {

    abstract fun execute(document: Document, fileFormat: FileFormat): Document

    protected fun getFileEnding(name: String): String {
        val split = name.split(Regex("\\.")).toTypedArray()
        return split[split.size - 1]
    }
}
