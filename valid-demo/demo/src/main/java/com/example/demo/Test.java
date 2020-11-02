package com.example.demo;

import com.example.demo.bean.Student;
import org.hibernate.validator.internal.metadata.descriptor.BeanDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.PropertyDescriptorImpl;
import org.springframework.util.CollectionUtils;

import javax.swing.text.html.parser.Entity;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Test {
	public static void main(String[] args) {
		//ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		//Validator validator =validatorFactory.getValidator();
		Student student = new Student();
		student.setId("1");
		student.setName("xj");
		student.setHasBook(true);
		student.setBookeName("java从入门到转行");
		beanValidate(student);
	}

	private static <T> void beanValidate(T object) {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		getDesc(validator);

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

	private static void getDesc (Validator validator){
		BeanDescriptor carDescriptor = validator.getConstraintsForClass(Student.class);


		//获取带有注解的字段
		Set<PropertyDescriptor> constrainedProperties = carDescriptor.getConstrainedProperties();
		constrainedProperties.forEach(propertyDescriptor->{
			System.out.println(propertyDescriptor.getPropertyName()+":");
			//获取字段的每个注解
			Set<ConstraintDescriptor<?>> constraintDescriptorImpls =  propertyDescriptor.getConstraintDescriptors();
			constraintDescriptorImpls.forEach(dto->{
				System.out.println("注解："+dto.getAnnotation().annotationType().getName());
				//获取注解的参数和值
				Map<String,Object> map = dto.getAttributes();
				System.out.println("--------------------------");
				for(Map.Entry<String, Object> entity: map.entrySet()){
					System.out.println(entity.getKey()+":"+entity.getValue().toString());
				}
				System.out.println("--------------------------");
			});
			System.out.println();
		});
	}
}
