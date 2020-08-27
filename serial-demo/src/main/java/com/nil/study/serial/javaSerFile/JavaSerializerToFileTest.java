package com.nil.study.serial.javaSerFile;

import com.nil.study.serial.User;
import com.nil.study.serial.inteface.ISerializer;

public class JavaSerializerToFileTest {
	public static void main(String[] args) {
		User user = new User();
		user.setName("zhangsan");
		ISerializer serializer = new JavaSerializerFile();
		byte[] bytes = serializer.serialize(user);
		System.out.println(bytes.length);
		for (int i=0;i<bytes.length;i++){
			System.out.print(bytes[i]+" ");
		}
		System.out.println();
	}
}
