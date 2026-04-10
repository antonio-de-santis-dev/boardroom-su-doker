package main.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(StaticResourceConfig.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
        File uploadDir = uploadPath.toFile();

        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            LOG.info("Cartella uploads creata: {} ({})", uploadPath, created ? "OK" : "ERRORE");
        }

        // toUri().toString() produce "file:///C:/..." su Windows e "file:/..." su Linux
        String resourceLocation = uploadPath.toUri().toString();
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }

        LOG.info("Serving /uploads/** from: {}", resourceLocation);

        registry.addResourceHandler("/uploads/**").addResourceLocations(resourceLocation).setCachePeriod(3600);
    }
}
