package com.cuupa.converter.services.command

import com.cuupa.converter.to.Document

abstract class Command {

    abstract fun execute(): Document
}