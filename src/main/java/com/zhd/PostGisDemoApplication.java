package com.zhd;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;


@MapperScan("com.zhd.mapper")
@EntityScan("com.zhd.pojo")
@SpringBootApplication
public class PostGisDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostGisDemoApplication.class, args);
    }

}