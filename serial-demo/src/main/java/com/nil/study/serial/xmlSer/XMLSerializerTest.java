package com.nil.study.serial.xmlSer;

import com.nil.study.serial.User;
import com.nil.study.serial.inteface.ISerializer;
import com.nil.study.serial.javaSer.JavaSerializer;

public class XMLSerializerTest {
	public static void main(String[] args) {
		User user = new User();
		user.setName("zhangsan");
		user.setAge(18);
		ISerializer serializer = new XMLSerializer();
		byte[] bytes = serializer.serialize(user);
		System.out.println(bytes.length);   //215
		System.out.println(new String(bytes));
		/*
		<com.nil.study.serial.User serialization="custom">
		  <com.nil.study.serial.User>
		    <default>
		      <name>zhangsan</name>
		    </default>
		    <int>18</int>
		  </com.nil.study.serial.User>
		</com.nil.study.serial.User>
		*/

		System.out.println();

		User userRevar = serializer.deserialize(bytes);
		System.out.println(userRevar);  // User{name='zhangsan', age=18}
	}
}
