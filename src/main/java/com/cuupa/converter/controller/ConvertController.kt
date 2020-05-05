package com.cuupa.converter.controller

import com.cuupa.converter.services.Converter
import com.cuupa.converter.services.command.FileFormat
import com.cuupa.converter.to.Document
import com.google.gson.Gson
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class ConvertController(private val converter: Converter) {

    @GetMapping(value = ["/api/rest/1.0/ping"])
    fun ping(): ResponseEntity<String> {
        return ResponseEntity.ok().body("200")
    }

    @PostMapping(value = ["/api/rest/1.0/convert"])
    fun convert(@RequestBody json: String?): ResponseEntity<String> {
        try {
            var document = gson.fromJson(json, Document::class.java)
            document = converter.convert(document!!)
            val result = gson.toJson(document)
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
    }

    @PostMapping(value = ["/api/rest/1.0/convert/target={target}"])
    fun convert(@RequestBody json: String?, @PathVariable target: String?): ResponseEntity<String> {
        if (!target.isNullOrBlank()) {
            print(json)
            val fileFormat: FileFormat = FileFormat.valueOf(target.toUpperCase())
            var document = gson.fromJson(json, Document::class.java)
            document = converter.convertTo(document!!, fileFormat)
            val result = gson.toJson(document)
            return ResponseEntity.ok(result)
        } else {
            return convert(json)
        }
    }

    companion object {
        private val gson = Gson()
    }
}