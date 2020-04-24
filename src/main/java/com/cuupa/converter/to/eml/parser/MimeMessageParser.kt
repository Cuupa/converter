package com.cuupa.converter.to.eml.parser

import com.cuupa.converter.to.eml.Attachment
import com.cuupa.converter.to.eml.Email
import com.google.common.io.BaseEncoding
import com.google.common.io.ByteStreams
import com.sun.mail.util.BASE64DecoderStream
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Multipart
import javax.mail.Part
import javax.mail.internet.ContentType
import javax.mail.internet.MimeMessage

object MimeMessageParser {
    // html wrapper template for text/plain messages
    private const val HTML_WRAPPER_TEMPLATE = "<!DOCTYPE html><html><head><style>body{font-size: 0.5cm;}</style><meta charset=\"%s\"><title>%s</title></head><body>%s</body></html>"

    val datetimeformatter = DateTimeFormatter.ofPattern("E, d MMM y HH:mm:ss Z", Locale.ENGLISH)

    @JvmStatic
    @Throws(Exception::class)
    fun parse(message: Message): Email {

        val email = Email(getSender(message),
                message.getRecipients(MimeMessage.RecipientType.TO),
                message.subject, getReceiveTime(message),
                isLotusNotesMessage(message))

        val inlineImageMap = getInlineImageMap(message)

        parseMimeStructure(message, object : MimeParserCallback {

            override fun callback(part: Part) {
                val isAttachment = Part.ATTACHMENT.equals(part.disposition, ignoreCase = true)
                if (!isValidPart(part) && !isAttachment) {
                    return
                }
                if (!isAttachment) {
                    var content = getStringContent(part)
                    val contentType = ContentType(part.contentType)
                    if (StringUtils.isNotBlank(content)) {
                        if (part.isMimeType(MimeType.TEXT_PLAIN.get())) {
                            email.setPlainText(content, contentType)
                        } else if (part.isMimeType(MimeType.TEXT_HTML.get())) {
                            content = parseHTML(content, email.subject, contentType, inlineImageMap)
                            email.setHtmlText(content, contentType)
                        }
                    }
                } else {
                    val fileName = part.fileName
                    if (StringUtils.isNotBlank(fileName)) {
                        part.inputStream.use { stream ->
                            val content = ByteArray(part.size)
                            stream.read(content)
                            email.addAttachment(Attachment(fileName, content, ContentType(part.contentType)))
                        }
                    }
                }
            }
        })
        return email
    }

    private fun getReceiveTime(message: Message): LocalDateTime {

        for (dateHeader in message.getHeader("Date")) {
            val date = removeTimeZoneInfo(dateHeader)
            return LocalDateTime.parse(date, datetimeformatter)
        }
        return LocalDateTime.MIN
    }

    private fun getSender(message: Message): String {
        if (message.from != null && message.from.isNotEmpty()) {
            return message.from[0].toString()
        }
        return ""
    }

    /**
     * Determine if this message is a Lotus Notes Message for special character encoding
     * @param message The Message
     * @return true if the message is a Lotus Notes message, false otherwise
     */
    private fun isLotusNotesMessage(message: Message): Boolean {
        try {
            for (header in message.allHeaders) {
                if (header.name.equals("X-Notes-Item", ignoreCase = true)) {
                    return true
                }
            }
        } catch (e: MessagingException) {
            return false
        }
        return false
    }

    /**
     * @param string
     * @return
     */
    private fun removeTimeZoneInfo(date: String): String {
        return if (date.contains("(") && date.contains(")")) {
            date.split(Regex("\\(")).toTypedArray()[0].trim { it <= ' ' }
        } else date
    }

    private fun parseMimeStructure(part: Part, callback: MimeParserCallback) {
        callback.callback(part)
        if (part.isMimeType(MimeType.MULTIPART.get())) {
            val mp = part.content as Multipart
            for (i in 0 until mp.count) {
                parseMimeStructure(mp.getBodyPart(i), callback)
            }
        }
    }

    private fun isValidPart(part: Part?): Boolean {
        return try {
            part != null && (part.isMimeType(MimeType.TEXT.get()) || part.isMimeType(MimeType.MULTIPART.get()))
        } catch (e: MessagingException) {
            false
        }
    }

    /**
     * Get the String Content of a MimePart.
     * @param p MimePart
     * @return Content as String
     * @throws IOException
     * @throws MessagingException
     */
    private fun getStringContent(part: Part?): String {
        if (part == null) {
            return ""
        }

        val content: Any? = try {
            part.content
        } catch (e: Exception) {
            // most likely the specified charset could not be found
            part.inputStream
        }

        if (content is String) {
            return content
        } else if (content is InputStream) {
            val contentType = ContentType(part.contentType)
            return String(ByteStreams.toByteArray(content), Charset.forName(contentType.getParameter("charset")))
        }
        return ""
    }

    /**
     * Get all inline images (images with an Content-Id) as a Hashmap. The key is the Content-Id and all images in all multipart
     * containers are included in the map.
     * @param p mime object
     * @return Hashmap&lt;Content-Id, &lt;Base64Image, ContentType&gt;&gt;
     * @throws Exception
     */
    private fun getInlineImageMap(part: Part): HashMap<String, MimeObjectEntry<String>> {
        val result = HashMap<String, MimeObjectEntry<String>>()

        parseMimeStructure(part, object : MimeParserCallback {

            override fun callback(part: Part) {
                if (part.isMimeType(MimeType.IMAGE.get()) && part.getHeader("Content-Id") != null) {
                    val id = part.getHeader("Content-Id")[0]
                    val b64ds = part.content as BASE64DecoderStream
                    val imageBase64 = BaseEncoding.base64().encode(ByteStreams.toByteArray(b64ds))
                    result[id] = MimeObjectEntry(imageBase64, ContentType(part.contentType))
                }
            }
        })
        return result
    }

    private fun parseHTML(content: String, subject: String, contentType: ContentType,
                          inlineImageMap: HashMap<String, MimeObjectEntry<String>>): String {
        var content = content
        if (contentType.match(MimeType.TEXT_HTML.get())) {
            if (inlineImageMap.size > 0) {

                // content = StringReplacer.replace(content, IMG_CID_REGEX, new StringReplacerCallback() {
                // @Override
                // public String replace(Matcher m) throws Exception {
                // MimeObjectEntry<String> base64Entry = inlineImageMap.get("<" + m.group(1) + ">");
                //
                // // found no image for this cid, just return the matches string as it is
                // if (base64Entry == null) {
                // return m.group();
                // }
                //
                // return "data:" + base64Entry.getContentType().getBaseType() + ";base64," + base64Entry.getEntry()
                // + "\"";
                // }
                // });
            }
        } else {

            // replace \n line breaks with <br>
            content = content.replace("\n", "<br>").replace("\r", "")

            // replace whitespace with &nbsp;
            content = content.replace(" ", "&nbsp;")
            content = String.format(HTML_WRAPPER_TEMPLATE, contentType.getParameter("charset"), subject, content)
            // if (inlineImageMap.size() > 0) {
            //
            // // find embedded images and embed them in html using <img src="data:image ...>
            // // syntax
            // content = StringReplacer.replace(content, IMG_CID_PLAIN_REGEX, new StringReplacerCallback() {
            //
            // @Override
            // public String replace(Matcher m) throws Exception {
            // MimeObjectEntry<String> base64Entry = inlineImageMap.get("<" + m.group(1) + ">");
            //
            // // found no image for this cid, just return the matches string
            // if (base64Entry == null) {
            // return m.group();
            // }
            //
            // return "<img src=\"data:" + base64Entry.getContentType().getBaseType() + ";base64,"
            // + base64Entry.getEntry() + "\" />";
            // }
            //
            // });
            // }
        }
        return content
    } //    public static List<Part> getAttachments(Part p) throws Exception {
    //        final List<Part> result = new ArrayList<Part>();
    //
    //        // walkMimeStructure(p, 0, new WalkMimeCallback() {
    //        // @Override
    //        // public void walkMimeCallback(Part p, int level) throws Exception {
    //        // if (Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())
    //        // || ((p.getDisposition() == null) && !Strings.isNullOrEmpty(p.getFileName())))
    //        // {
    //        // result.add(p);
    //        // }
    //        // }
    //        // });
    //
    //        return result;
    //    }
}