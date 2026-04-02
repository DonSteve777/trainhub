package com.trainhub.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/avatars}")
    private String uploadDir;

    /**
     * cualquier peticion que empiece por /uploads/ 
     * file: -> indica que es una ruta del filesystem, no del classpath (por defecto,
     * spring boot sirve solo recursos estáticos desde ubicaciones predefinidas dentro del classpath)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")  
                .addResourceLocations("file:uploads/");
    }
}
