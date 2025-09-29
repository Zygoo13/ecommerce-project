package com.zygoo13.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Đường dẫn tuyệt đối tới backend/user-photos
        String userPhotosPath = System.getProperty("user.dir")
                + "/ecommerce-web-parent/ecommerce-backend/user-photos/";

        registry.addResourceHandler("/user-photos/**")
                .addResourceLocations("file:" + userPhotosPath);
    }


}
