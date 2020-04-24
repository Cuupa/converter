package com.cuupa.converter.to.eml

import java.time.LocalDateTime
import javax.mail.Address
import javax.mail.internet.ContentType

/**
 * The flag lotusNotesMessage sets a flag for the proprietary Lotus Notes encoding for special characters like 'ä', 'ü' which is encoded as
 * '0xL184z' and '0xL181z' respectivly
 */
data class Email(val sender: String, val recipients: Array<Address>, val subject: String, val receiveTime:
LocalDateTime, val lotusNotesMessage: Boolean) {

    private val plaintextMimePart = PlainTextMimePart()
    private val htmlMimePart = HtmlMimePart()

    private val attachments = mutableListOf<Attachment>()

    fun setPlainText(content: String?, contentType: ContentType?) {
        plaintextMimePart.setContent(content, contentType)
    }

    fun setHtmlText(content: String?, contentType: ContentType?) {
        htmlMimePart.setContent(content, contentType)
    }

    /**
     * @return
     */
    val plainText: String?
        get() = plaintextMimePart.content

    val htmlText: String?
        get() = htmlMimePart.content

    /**
     *
     * @param lotusNotesMessage The parameter wether it's a Lotus Notes message or not
     */

    /**
     * If this message is a document from Lotus Notes, then there could be a possibility of mailheaders containing incorrect
     * decoded special characters TODO: There are definitely more special characters than 'ä', 'ö' and 'ü' ...
     * @param text The text to decode
     * @return The decoded text
     */
    private fun decodeText(text: String): String {
        return if (lotusNotesMessage) {
            text.replace("0xL184z", "ä").replace("0xL181z", "ü").replace("0xL194z", "ö")
        } else text
    }

    fun addAttachment(attachment: Attachment) {
        attachments.add(attachment)
    }
}