package com.nil.pagehelp1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = {"com.nil.pagehelp1.dao"})
public class PageHelpApplication {

	public static void main(String[] args) {
		SpringApplication.run(PageHelpApplication.class, args);
	}

}
