package com.cuupa.converter.services.command.clean

import com.cuupa.converter.to.Document

abstract class CleanCommand : Command() {

    abstract fun isApplicable(document: Document): Boolean
}