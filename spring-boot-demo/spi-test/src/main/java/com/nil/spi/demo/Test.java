package com.nil.spi.demo;

import com.nil.spi.DataBaseDriver;
import java.util.ServiceLoader;

public class Test {
	public static void main(String[] args) {
		//可以获取到DataBaseDriver接口的所有实现类
		ServiceLoader<DataBaseDriver> serviceLoader = ServiceLoader.load(DataBaseDriver.class);
		//打印出所有的实现类
		for(DataBaseDriver dataBaseDriver : serviceLoader){
			System.out.println(dataBaseDriver.connect("test"));
		}
	}
}
