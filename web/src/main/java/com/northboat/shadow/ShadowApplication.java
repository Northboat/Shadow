package com.northboat.shadow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@MapperScan("com.northboat.shadow.mapper")
@EnableWebSocket
public class ShadowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShadowApplication.class, args);
    }

}
