package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SocketClientTest {
	public static void main(String args[]) {
		// 为了简单起见，所有的异常都直接往外抛
		String host = "192.168.45.55"; // 要连接的服务端IP地址
		int port = 8080; // 要连接的服务端对应的监听端口
		// 与服务端建立连接
		try {
			Socket socket = new Socket(host, port);
			// 建立连接后就可以往服务端写数据了
			Writer writer = new OutputStreamWriter(socket.getOutputStream());
			writer.write("Hello Server.");
			writer.flush();// 写完后要记得flush

			// 接收
			System.out.println("开始接收");
			// BufferedReader br = new BufferedReader(new
			// InputStreamReader(socket.getInputStream(), "UTF-8"));
			// System.out.println("从服务器收到 :" + br.readLine());

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			char[] aaa = new char[100];
//			System.out.println(in.readLine());
			System.out.println(in.read(aaa));
			// for (int i = 0; i < 4; i++)
			// System.out.println(in.read());
			System.out.println(aaa);
			in.close();

			System.out.println("接收完毕");

			// 关闭流
			// br.close();

			writer.close();
			socket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
