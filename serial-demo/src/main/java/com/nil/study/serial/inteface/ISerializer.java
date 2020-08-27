package com.nil.study.serial.inteface;

public interface ISerializer {

	<T> byte[] serialize(T obj);

	<T> T deserialize(byte[] data);
}
