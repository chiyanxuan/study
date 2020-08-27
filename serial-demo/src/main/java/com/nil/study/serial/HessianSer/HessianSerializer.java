package com.nil.study.serial.HessianSer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.nil.study.serial.inteface.ISerializer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerializer implements ISerializer {

	public <T> byte[] serialize(T obj) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
		try {
			hessianOutput.writeObject(obj);
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	public <T> T deserialize(byte[] data) {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
		HessianInput hessianInput = new HessianInput(byteArrayInputStream);
		try {
			return (T)hessianInput.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
