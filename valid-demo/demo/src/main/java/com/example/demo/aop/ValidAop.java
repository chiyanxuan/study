package com.example.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

@Aspect
@Component
public class ValidAop {

	/*@Pointcut("execution(public * org.hibernate.validator.internal.engine.ValidatorImpl.validate(..))")
	public void process() {}*/

	/*@Pointcut("execution(public * com.example.demo.controller..*.*(..))")
	public void process(){}*/

	/*@Pointcut("execution(* com.example.demo..*.hello(..))")
	public void process(){}*/

	/*@Pointcut("execution(* com.example.demo.controller.HelloWorldController.*(..))")
	public void process(){}*/

	/*@Pointcut("execution(* org.hibernate.validator..*.*(..))")
	public void process(){}*/

	@Pointcut("execution(public * org.springframework.boot.autoconfigure.validation.ValidatorAdapter.validate(..))")
	public void process(){}


	@Around("process()")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		System.out.println("方法开始时间是:"+new Date());
		Object o = joinPoint.proceed();
		System.out.println("方法结束时间是:"+new Date()) ;
		return null;
	}

	private static <T> void beanValidate(T object) {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		//getDesc(validator);

		//ExecutableValidator executableValidator = validator.forExecutables();
		//executableValidator.validateParameters(object,)
		// 校验单个属性
		//Set<ConstraintViolation<T>> constraintViolationSet = validator.validateProperty(object,"hasBook");


		//校验整个实体
		Set<ConstraintViolation<T>> constraintViolationSet = validator.validate(object);
		if(!CollectionUtils.isEmpty(constraintViolationSet)){
			Iterator<ConstraintViolation<T>> iterator = constraintViolationSet.iterator();
			while(iterator.hasNext()){
				System.out.println(iterator.next().getMessage());
			}
		}
	}

	/*@Before("process()")
	public void doBefore(ProceedingJoinPoint joinPoint){
		System.out.println("doBefore");
	}

	@After("process()")
	public Object doAfter(ProceedingJoinPoint joinPoint){
		System.out.println("doAfter");
	}*/
}
