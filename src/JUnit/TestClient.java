package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Test;

import protocol.RegisterMsg;
import server.NetworkMessage;

import client.SocketClientTest;

public class TestClient {
//	String host = "192.168.45.11"; // 要连接的服务端IP地址
	 String host = "192.168.45.17"; // 要连接的服务端IP地址
	int port = 8080; // 要连接的服务端对应的监听端口

	public Socket socket;
	public InputStream inputStream;
	public OutputStream outputStream;
	public SocketClientTest client;

	private void link() throws IOException {
		socket = new Socket(host, port);
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
	}

	@Test
	public void testRegister() {
		try {
			client = new SocketClientTest();
			client.link();
			
			String randomData = (((int)(Math.random() * 100000)) + "").substring(0, 5);
			byte[] resultBytes = client.testRegisterCase(randomData, randomData, randomData);
			RegisterMsg.RegisterRsp responseObject = RegisterMsg.RegisterRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
			
			assertEquals(responseObject.getResultCode().toString(), RegisterMsg.RegisterRsp.ResultCode.SUCCESS.toString());
			
			resultBytes = client.testRegisterCase(randomData, randomData, randomData);
			responseObject = RegisterMsg.RegisterRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
			assertEquals(responseObject.getResultCode().toString(), RegisterMsg.RegisterRsp.ResultCode.USER_EXIST.toString());
//			assertEquals(responseObject.getResultCode().toString(), RegisterMsg.RegisterRsp.ResultCode.SUCCESS.toString());
//			String resutString = resultBytes
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLogin() {
		try {
			client = new SocketClientTest();
			client.link();
			
			String randomData = (((int)(Math.random() * 100000)) + "").substring(0, 5);
			byte[] resultBytes = client.testRegisterCase(randomData, randomData, randomData);
			RegisterMsg.RegisterRsp responseObject = RegisterMsg.RegisterRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
			
			assertEquals(responseObject.getResultCode().toString(), RegisterMsg.RegisterRsp.ResultCode.SUCCESS.toString());
			
			resultBytes = client.testRegisterCase(randomData, randomData, randomData);
			responseObject = RegisterMsg.RegisterRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
			assertEquals(responseObject.getResultCode().toString(), RegisterMsg.RegisterRsp.ResultCode.USER_EXIST.toString());
//			assertEquals(responseObject.getResultCode().toString(), RegisterMsg.RegisterRsp.ResultCode.SUCCESS.toString());
//			String resutString = resultBytes
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
