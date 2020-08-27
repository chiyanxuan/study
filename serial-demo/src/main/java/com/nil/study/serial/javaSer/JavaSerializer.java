package com.nil.study.serial.javaSer;

import com.nil.study.serial.inteface.ISerializer;

import java.io.*;

public class JavaSerializer implements ISerializer {
	public <T> byte[] serialize(T obj) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
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
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);

		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			return (T)objectInputStream.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				byteArrayInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
