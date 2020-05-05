package com.cuupa.converter.services.command.convert

import com.cuupa.converter.services.command.FileFormat
import com.cuupa.converter.to.Document
import com.cuupa.converter.to.DocumentBuilder.Companion.create
import com.cuupa.converter.to.eml.parser.MimeMessageParser.parse
import com.google.common.base.Joiner
import com.google.common.base.Strings
import com.google.common.html.HtmlEscapers
import com.google.common.io.Files
import com.google.common.io.Resources
import org.w3c.tidy.Tidy
import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeUtility

class EMLToPdfCommand : ConvertCommand() {

    override fun isApplicable(document: Document, fileFormat: FileFormat): Boolean {
        return getFileEnding(document.filename).equals("eml", ignoreCase = true)
    }

    override fun execute(document: Document, fileFormat: FileFormat): Document {
        var message: MimeMessage? = null
        ByteArrayInputStream(document.content).use { inputStream -> message = MimeMessage(null, inputStream) }
        val email = parse(message!!)
        //		String[] recipients = getRecipients(message);
//		String sentDateStr = message.getHeader("date", null);
//		String subject = message.getSubject();
//		String from = message.getHeader("From", null);
//		createHeader(recipients, sentDateStr, subject, from);
        val htmlBody = email.htmlText
        val tmpXhtml = File(document.filename.replace(".eml", ".xhtml"))
        tmpXhtml.parentFile.mkdirs()
        ByteArrayInputStream(htmlBody!!.toByteArray()).use { `in` -> FileOutputStream(tmpXhtml).use { out -> parseDocument(`in`, out) } }
        val pdfFile = render(tmpXhtml, document.filename)
        var cleanedDocument: Document? = null
        FileInputStream(pdfFile).use { `in` ->
            cleanedDocument = create(`in`, pdfFile.name, pdfFile.length().toInt())
                    .build()
        }
        tmpXhtml.delete()
        pdfFile.delete()
        return cleanedDocument!!
    }

    private fun render(tmpXhtml: File, filename: String): File {
        val pdfFile = File(filename.replace(".eml", ".pdf"))
        val os: OutputStream = FileOutputStream(pdfFile)
        val renderer = ITextRenderer()
        renderer.setDocument(tmpXhtml)
        renderer.layout()
        renderer.createPDF(os)
        renderer.finishPDF()
        os.close()
        return pdfFile
    }

    private fun getRecipients(message: MimeMessage): Array<String> {
        var recipients = arrayOf("")
        var recipientsRaw = message.getHeader("To", null)
        if (recipientsRaw != null && recipientsRaw.isNotEmpty()) {
            try {
                recipientsRaw = MimeUtility.unfold(recipientsRaw)
                recipients = recipientsRaw.split(",").toTypedArray()
                for (i in recipients.indices) {
                    recipients[i] = MimeUtility.decodeText(recipients[i])
                }
            } catch (e: Exception) {
            }
        }
        return recipients
    }

    private fun createHeader(recipients: Array<String>, sentDateStr: String,
                             subject: String, from: String) {
        val tmpHtmlHeader: File
        // if (!hideHeaders) {
        tmpHtmlHeader = File.createTempFile("emailtopdf", ".html")
        val tmpHtmlHeaderStr = Resources.toString(Resources.getResource("header.html"), StandardCharsets.UTF_8)
        var headers = ""
        if (!Strings.isNullOrEmpty(from)) {
            headers += String.format(HEADER_FIELD_TEMPLATE, "From", HtmlEscapers.htmlEscaper()
                    .escape(from))
        }
        if (!Strings.isNullOrEmpty(subject)) {
            headers += String.format(HEADER_FIELD_TEMPLATE, "Subject",
                    "<b>" + HtmlEscapers.htmlEscaper().escape(subject) + "<b>")
        }
        if (recipients.isNotEmpty()) {
            headers += String.format(HEADER_FIELD_TEMPLATE, "To",
                    HtmlEscapers.htmlEscaper().escape(Joiner.on(", ").join(recipients)))
        }
        if (!Strings.isNullOrEmpty(sentDateStr)) {
            headers += String.format(HEADER_FIELD_TEMPLATE, "Date", HtmlEscapers.htmlEscaper()
                    .escape(sentDateStr))
        }
        Files.write(String.format(tmpHtmlHeaderStr, headers), tmpHtmlHeader, StandardCharsets.UTF_8)

        // Append this script tag dirty to the bottom
        // htmlBody += String.format(ADD_HEADER_IFRAME_JS_TAG_TEMPLATE,
        // tmpHtmlHeader.toURI(),
        // Resources.toString(Resources.getResource("contentScript.js"),
        // StandardCharsets.UTF_8));
        // }
    }

    private fun parseDocument(inputStream: InputStream, outputStream: OutputStream) {
        val tidy = Tidy()
        tidy.quiet = false
        tidy.showWarnings = true
        tidy.showErrors = 0
        tidy.makeClean = true
        tidy.forceOutput = true
        tidy.xhtml = true
        //// tidy.setXmlTags(true);
        tidy.inputEncoding = StandardCharsets.UTF_8.name()
        //		tidy.setOutputEncoding("UTF-8");
        tidy.parseDOM(inputStream, outputStream)
    }

    companion object {
        //	private static final String ADD_HEADER_IFRAME_JS_TAG_TEMPLATE = "<script id=\"header-v6a8oxpf48xfzy0rhjra\" data-file=\"%s\" type=\"text/javascript\">%s</script>";
        private const val HEADER_FIELD_TEMPLATE = "<tr><td class=\"header-name\">%s</td><td class=\"header-value\">%s</td></tr>"
        private val IMG_CID_PLAIN_REGEX = Pattern.compile("\\[cid:(.*?)\\]", Pattern.DOTALL)
        private val IMG_CID_REGEX = Pattern.compile("cid:(.*?)\"", Pattern.DOTALL)
    }

}