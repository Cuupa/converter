package com.cuupa.converter.configuration.spring

import com.cuupa.converter.controller.ConvertController
import com.cuupa.converter.services.Converter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class ApplicationConfiguration {

    @Bean
    open fun converter(): Converter {
        return Converter()
    }

    @Bean
    open fun convertController(): ConvertController {
        return ConvertController(converter())
    }
}