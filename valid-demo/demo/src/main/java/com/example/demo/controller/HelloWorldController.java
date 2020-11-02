package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.example.demo.bean.Student;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

@RestController
public class HelloWorldController {

	@PostMapping(value = "/hello")
	public String hello(@RequestBody @Valid Student student){
		System.out.println("student:"+ JSON.toJSONString(student));
		return "success";
	}

	private <T> void beanValidate(T object) {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<T>> constraintViolationSet = validator.validate(object);
		if(CollectionUtils.isEmpty(constraintViolationSet)){
			String validateStrCode = constraintViolationSet.iterator().next().getMessage();
			System.out.println(validateStrCode);
		}
	}
}
