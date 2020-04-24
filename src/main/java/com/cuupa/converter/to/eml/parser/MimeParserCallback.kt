package com.cuupa.converter.to.eml.parser

import javax.mail.Part

interface MimeParserCallback {

    fun callback(part: Part)
}