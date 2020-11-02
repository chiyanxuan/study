package com.tencent.valid;

import com.tencent.validator.MyValidatorClass;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MyValidatorClass.class)
public @interface MyValidator {
	String message();

	String name();

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}