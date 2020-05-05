package com.cuupa.converter.services.command

enum class FileFormat(name: String?) {

    PDF("pdf"), XHTML("xhtml"), DOCX("docx"), UNKNOWN("");

    fun get(name: String?): FileFormat {
        return if (name.isNullOrBlank()) {
            UNKNOWN
        } else {
            when (name) {
                "pdf" -> PDF
                "xhtml" -> XHTML
                "docx" -> DOCX
                else -> UNKNOWN
            }
        }
    }
}
