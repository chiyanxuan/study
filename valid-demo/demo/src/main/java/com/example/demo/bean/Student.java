package com.example.demo.bean;


import com.tencent.valid.MyValidator;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

@Data
public class Student {
	@NotBlank
	@MyValidator(name = "id", message = "ID的自定义校验")
	private String id;

	@NotNull
	private String name;

	@AssertTrue
	private Boolean hasBook;

	@NotBlank
	private String school;

	@NotBlank
	private String bookeName;

}
