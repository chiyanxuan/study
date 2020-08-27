package com.nil.study.serial.javaSer;

import com.nil.study.serial.User;
import com.nil.study.serial.inteface.ISerializer;

public class JavaSerializerTest {
	public static void main(String[] args) {
		User user = new User();
		user.setName("zhangsan");
		ISerializer serializer = new JavaSerializer();
		byte[] bytes = serializer.serialize(user);
		System.out.println(bytes.length);   //85
		for (int i=0;i<bytes.length;i++){
			System.out.print(bytes[i]+" ");
		}
		//-84 -19 0 5 115 114 0 25 99 111 109 46 110 105 108 46 115 116 117 100 121 46 115 101 114 105 97 108 46 85 115 101 114 3 -93 113 -90 34 -121 -56 -64 2 0 1 76 0 4 110 97 109 101 116 0 18 76 106 97 118 97 47 108 97 110 103 47 83 116 114 105 110 103 59 120 112 116 0 8 122 104 97 110 103 115 97 110
		System.out.println();

		User userRevar = serializer.deserialize(bytes);
		System.out.println(userRevar);  // User{name='zhangsan'}
	}
}
