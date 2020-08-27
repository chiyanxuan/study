package com.nil.study.serial;

import sun.awt.geom.AreaOp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketDemo {
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(8080);
			Socket socket = serverSocket.accept();
			ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
			User user = (User) objectInputStream.readObject();
			System.out.println(user);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {

		}
	}
}
