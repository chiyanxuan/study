package com.nil.study.serial.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.nil.study.serial.UserProto;
import com.google.protobuf.ByteString;

public class ProtobufMain {

	public static void main(String[] args) {

		try {
			UserProto.User user = UserProto.User.newBuilder().setName("nil").setAge(300).build();
			ByteString bytes = user.toByteString();
			System.out.println(bytes.size());  // 7

			for(byte bt : bytes.toByteArray()){
				System.out.print(bt +" ");
			}
			// tag lenth  n   i    l   tag   18
			// 10   3    110 105  108  16    18
			// tag = field num << 3 | WIRE_TYPE
			//field num表示字段是第几个，这是写在User.proto里面的
			// WIRE_TYPE是约定的值，和字段类型有关，可以在网上搜一下，有个表格
			//   10 = 1<<3 | 2 = 10
			//   16 = 2<<3 | 0 =16
			//int型的length省略了
			System.out.println();
			UserProto.User userRever = UserProto.User.parseFrom(bytes);
			System.out.println(userRever);
				/*
				name: "nil"
				age: 18
				 */
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
}
