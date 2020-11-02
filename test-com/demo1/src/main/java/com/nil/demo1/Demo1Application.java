package com.nil.demo1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.nil")
public class Demo1Application {

    public Demo1Application(PlugLoadComponent plugLoadComponent) {
        plugLoadComponent.register();
    }

    public static void main(String[] args) {
        SpringApplication.run(Demo1Application.class, args);
    }

}
