package com.nil.spi;

public class MsqlDriver implements DataBaseDriver{
	@Override
	public String connect(String arg) {
		return "mysql:"+arg;
	}
}
