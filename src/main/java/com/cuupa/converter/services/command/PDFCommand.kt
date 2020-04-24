package com.cuupa.converter.services.command

import com.cuupa.converter.to.Document
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentCatalog
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript
import org.apache.pdfbox.pdmodel.interactive.action.PDActionMovie
import org.apache.pdfbox.pdmodel.interactive.action.PDFormFieldAdditionalActions
import org.apache.pdfbox.pdmodel.interactive.form.PDField
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField
import org.apache.pdfbox.pdmodel.interactive.form.PDTerminalField
import java.io.ByteArrayOutputStream
import java.io.IOException

class PDFCommand(private val document: Document) : Command() {

    private val blacklistedFiles = listOf(".exe", ".bat", ".js", ".py", ".app", ".sh", ".dmg")

    override fun execute(): Document {
        try {
            PDDocument.load(document.content).use { pdf ->
                processDocument(pdf)
                val byteArrayOutputStream = ByteArrayOutputStream()
                pdf.save(byteArrayOutputStream)
                document.content = byteArrayOutputStream.toByteArray()
            }
        } catch (ipe: InvalidPasswordException) {
            try {
                PDDocument.load(document.content, "").use { pdf -> processDocument(pdf) }
            } catch (ipe2: InvalidPasswordException) {
                document.encrypted = true
            }
        }
        return document
    }

    @Throws(IOException::class)
    private fun processDocument(pdf: PDDocument) {
        var documentCatalog = pdf.documentCatalog
        documentCatalog = removeEmbeddedFiles(documentCatalog)
        documentCatalog = removeCatalogActions(documentCatalog);
        documentCatalog = removeFormActions(documentCatalog)
        pdf.isAllSecurityToBeRemoved = true
    }

    private fun removeFormActions(documentCatalog: PDDocumentCatalog): PDDocumentCatalog {
        val acroForm = documentCatalog.acroForm
        if (acroForm != null) {
            val fields = acroForm.fields
            for (pdField in fields) {
                processField(pdField)
            }
        }
        return documentCatalog
    }

    private fun removeCatalogActions(documentCatalog: PDDocumentCatalog): PDDocumentCatalog {
        var documentCatalogLocal = documentCatalog
        documentCatalogLocal.openAction = null
        documentCatalogLocal = removeActions(documentCatalogLocal)
        return documentCatalogLocal
    }

    private fun removeEmbeddedFiles(documentCatalog: PDDocumentCatalog): PDDocumentCatalog {
        val names = documentCatalog.names ?: return documentCatalog
        val embeddedFiles = names.embeddedFiles ?: return documentCatalog
        val map = embeddedFiles.names
        for ((_, fileSpecification) in map) {
            if (isAllowedFile(fileSpecification.embeddedFile.file.file)) {
            }
        }
        return documentCatalog
    }

    private fun isAllowedFile(file: String): Boolean {
        for (blacklistedFile in blacklistedFiles) {
            return !file.endsWith(blacklistedFile)
        }
        return true
    }

    private fun removeActions(documentCatalog: PDDocumentCatalog): PDDocumentCatalog {
        return try {
            val actions = documentCatalog.actions
            actions.dp = null
            actions.ds = null
            actions.wc = null
            actions.wp = null
            actions.ws = null
            documentCatalog
        } catch (ex: Exception) {
            documentCatalog
        }
    }

    private fun processField(field: PDField) {
        if (field is PDTerminalField) {
            var fieldActions = field.getActions()
            if (fieldActions != null) {
                fieldActions = removeJavaScriptActions(fieldActions)
            }
            for (widgetAction in field.widgets) {
                val action = widgetAction.action
                if (action is PDActionJavaScript) {
                    widgetAction.action = null
                } else if (action is PDActionMovie) {
                    widgetAction.action = null
                }
            }
        }
        if (field is PDNonTerminalField) {
            for (child in field.children) {
                processField(child)
            }
        }
    }

    private fun removeJavaScriptActions(fieldActions: PDFormFieldAdditionalActions): PDFormFieldAdditionalActions {
        fieldActions.k = null
        fieldActions.c = null
        fieldActions.f = null
        fieldActions.v = null
        return fieldActions
    }
}