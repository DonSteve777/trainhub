package com.trainhub.backend.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/avatars}")
    private String uploadDir;

    /**
     * Sirve /uploads/** desde el directorio padre de {@code app.upload.dir}
     * (p. ej. uploads/avatars → file:.../uploads/), para que coincida con la escritura.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path avatarsAbs = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path uploadsRoot = avatarsAbs.getParent();
        if (uploadsRoot == null) {
            uploadsRoot = avatarsAbs;
        }
        // file:///.../uploads/  (barra final obligatoria para ResourceHandler)
        String location = uploadsRoot.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
