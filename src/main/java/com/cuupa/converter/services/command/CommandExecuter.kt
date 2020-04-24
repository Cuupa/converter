package com.cuupa.converter.services.command

import com.cuupa.converter.to.Document

object CommandExecuter {

    fun execute(command: Command?): Document? {
        return command?.execute()
    }
}