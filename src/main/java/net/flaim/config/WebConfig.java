package net.flaim.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig {

    @Value("${app.skins.directory}")
    private String skinsDirectory;

    @Value("${app.avatars.directory}")
    private String avatarsDirectory;

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/skins/**")
                        .addResourceLocations("file:" + Paths.get(skinsDirectory).toAbsolutePath() + "/")
                        .setCachePeriod(0);

                registry.addResourceHandler("/avatars/**")
                        .addResourceLocations("file:" + Paths.get(avatarsDirectory).toAbsolutePath() + "/")
                        .setCachePeriod(0);
            }

        };
    }
}