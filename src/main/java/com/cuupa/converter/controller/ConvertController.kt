package com.cuupa.converter.controller

import com.cuupa.converter.services.Converter
import com.cuupa.converter.to.Document
import com.google.gson.Gson
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class ConvertController(private val converter: Converter) {

    @RequestMapping(value = ["/ping"], method = [RequestMethod.GET])
    fun ping(): ResponseEntity<String> {
        return ResponseEntity.ok().body("200")
    }

    @RequestMapping(value = ["/convert"], method = [RequestMethod.POST])
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

    companion object {
        private val gson = Gson()
    }

}