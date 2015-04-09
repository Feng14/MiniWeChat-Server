package test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import client.SocketClientTest;

public class Client111 {
	public static final String host = "127.0.0.1"; // 要连接的服务端IP地址
	int port = 8081; // 要连接的服务端对应的监听端口

	public static final int HEAD_INT_SIZE = 4;
	public Socket socket;
	public InputStream inputStream;
	public OutputStream outputStream;

	public static void main(String args[]) throws IOException {
		new Client111();
	}

	public Client111() throws UnknownHostException, IOException {
		link();
	}

	public void link() throws UnknownHostException, IOException {
		socket = new Socket(host, port);
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
		
		byte[] byteArray = new byte[100];
		while (true) {
			inputStream.read(byteArray);
			System.out.println(byteArray);
		}
	}

}
