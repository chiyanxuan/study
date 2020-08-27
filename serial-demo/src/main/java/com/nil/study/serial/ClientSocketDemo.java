package com.nil.study.serial;

import javax.jws.soap.SOAPBinding;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientSocketDemo {
	public static void main(String[] args) {
		try {
			Socket socket = new Socket("localhost",8080);
			User user = new User();
			user.setName("zhangsan");
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectOutputStream.writeObject(user);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//TODO
		}
	}
}
