package com.zygoo13.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan({"com.zygoo13.common.entity", "com.zygoo13.admin.user"})
public class EcommerceBackendApplication {
    public static void main(String[] args) {

        SpringApplication.run(EcommerceBackendApplication.class, args);
    }
}
