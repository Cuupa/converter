package com.cuupa.converter.to.eml.parser

enum class MimeType(private val value: String) {
    TEXT("text/*"), TEXT_PLAIN("text/plain"), TEXT_HTML("text/html"), MULTIPART("multipart/*"), IMAGE("image/*");

    /**
     * @return the value
     */
    fun get(): String {
        return value
    }

}