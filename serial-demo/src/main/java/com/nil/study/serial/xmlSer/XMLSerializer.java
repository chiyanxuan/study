package com.nil.study.serial.xmlSer;

import com.nil.study.serial.inteface.ISerializer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XMLSerializer implements ISerializer {
	XStream xStream = new XStream(new DomDriver());

	public <T> byte[] serialize(T obj) {
		return xStream.toXML(obj).getBytes();
	}

	public <T> T deserialize(byte[] data) {
		return (T)xStream.fromXML(new String(data));
	}
}
