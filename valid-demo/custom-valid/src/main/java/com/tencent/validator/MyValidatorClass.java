package com.tencent.validator;

import com.tencent.valid.MyValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * MyValidator，自定义注解
 * String 标注在String类型的字段上，传过来的值是String类型
 */
public class MyValidatorClass implements ConstraintValidator<MyValidator,Object> {
	private String name;
	private String message;
	/**
	 * 初始化注解时执行的
	 * @param myValidator
	 */
	@Override
	public void initialize(MyValidator myValidator) {
		this.message = myValidator.message();
		this.name = myValidator.name();
	}

	/**
	 * 真正的校验逻辑
	 * @param o
	 * @param constraintValidatorContext
	 * @return
	 */
	@Override
	public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
		System.out.println("MyValidatorClass:对象："+o);
		return false;
	}
}
