package com.nil.study.serial.javaSer;

import com.nil.study.serial.User;
import com.nil.study.serial.inteface.ISerializer;

public class JavaSerializerTransientTest {
	// 被transient修饰的属性不会被序列化
	// 但是可以通过写 writeObject和readObject方法 手动代码序列化被transient修饰的属性，以实现自己想要加的某些逻辑
	public static void main(String[] args) {
		User user = new User();
		user.setName("zhangsan");
		user.setAge(18);
		ISerializer serializer = new JavaSerializer();
		byte[] bytes = serializer.serialize(user);
		System.out.println(bytes.length);   //85   // 加上writeObject和readObject方法以后  92
		for (int i=0;i<bytes.length;i++){
			System.out.print(bytes[i]+" ");
		}
		//-84 -19 0 5 115 114 0 25 99 111 109 46 110 105 108 46 115 116 117 100 121 46 115 101 114 105 97 108 46 85 115 101 114 0 0 0 0 0 0 0 1 2 0 1 76 0 4 110 97 109 101 116 0 18 76 106 97 118 97 47 108 97 110 103 47 83 116 114 105 110 103 59 120 112 116 0 8 122 104 97 110 103 115 97 110
		// 加上writeObject和readObject方法以后 -84 -19 0 5 115 114 0 25 99 111 109 46 110 105 108 46 115 116 117 100 121 46 115 101 114 105 97 108 46 85 115 101 114 0 0 0 0 0 0 0 1 3 0 1 76 0 4 110 97 109 101 116 0 18 76 106 97 118 97 47 108 97 110 103 47 83 116 114 105 110 103 59 120 112 116 0 8 122 104 97 110 103 115 97 110 119 4 0 0 0 18 120
		System.out.println();

		User userRevar = serializer.deserialize(bytes);
		System.out.println(userRevar);  // User{name='zhangsan', age=0}     age是int的默认值
		// 加上writeObject和readObject方法以后 User{name='zhangsan', age=18}
	}
}
