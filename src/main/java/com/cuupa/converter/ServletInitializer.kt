package com.cuupa.converter

import com.cuupa.converter.configuration.spring.SpringBootConverterApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

class ServletInitializer : SpringBootServletInitializer() {

    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(SpringBootConverterApplication::class.java)
    }
}