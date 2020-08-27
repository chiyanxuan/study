package com.nil.study.serial.javaSerFile;

import com.nil.study.serial.inteface.ISerializer;

import java.io.*;

public class JavaSerializerFile implements ISerializer {
	public <T> byte[] serialize(T obj) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			// 将文件读取到内存，作为输出
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("user"));
			// 将对象写入user文件
			objectOutputStream.writeObject(obj);
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				byteArrayOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new byte[0];
	}

	public <T> T deserialize(byte[] data) {
		try {
			// 从文件中读入内存
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File("user")));
			// 从内存中读对象类
			return (T)objectInputStream.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
		}
		return null;
	}
}
