package com.cuupa.converter.to.eml

import javax.mail.internet.ContentType

class HtmlMimePart {
    var content: String? = null
        private set
    var contentType: ContentType? = null
        private set

    fun setContent(content: String?, contentType: ContentType?) {
        this.content = content
        this.contentType = contentType
    }

}