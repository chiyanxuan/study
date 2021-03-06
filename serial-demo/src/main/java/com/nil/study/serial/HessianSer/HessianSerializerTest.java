package com.nil.study.serial.HessianSer;

import com.nil.study.serial.User;
import com.nil.study.serial.inteface.ISerializer;

public class HessianSerializerTest {
	public static void main(String[] args) {
		User user = new User();
		user.setName("zhangsan");
		user.setAge(18);
		ISerializer serializer = new HessianSerializer();
		byte[] bytes = serializer.serialize(user);
		System.out.println(bytes.length);   //48
		System.out.println(new String(bytes));  // Mt com.nil.study.serial.UserS nameSzhangsanz

		System.out.println();

		User userRevar = serializer.deserialize(bytes);
		System.out.println(userRevar);  // User{name='zhangsan', age=0}
		//writeObject 和 readObject 是java的，所以这里不生效
	}
}
