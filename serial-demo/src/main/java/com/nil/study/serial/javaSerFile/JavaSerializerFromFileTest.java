package com.nil.study.serial.javaSerFile;

import com.nil.study.serial.User;
import com.nil.study.serial.inteface.ISerializer;

public class JavaSerializerFromFileTest {
	public static void main(String[] args) {
		//反序列化的时候会对比serialVersionUID，如果不相同会报错的
		// serialVersionUID不写的时候会自动生成一个
		ISerializer serializer = new JavaSerializerFile();
		User userRevar = serializer.deserialize(null);
		System.out.println(userRevar);  // User{name='zhangsan'}
	}
}
