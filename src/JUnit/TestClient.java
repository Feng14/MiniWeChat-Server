package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.LoginMsg;
import protocol.RegisterMsg;
import server.NetworkMessage;

import client.SocketClientTest;

public class TestClient {
	// String host = "192.168.45.11"; // 要连接的服务端IP地址
	String host = "192.168.45.17"; // 要连接的服务端IP地址
	int port = 8080; // 要连接的服务端对应的监听端口

	public Socket socket;
	public InputStream inputStream;
	public OutputStream outputStream;
	public SocketClientTest client;

	@Before
	public void init() throws UnknownHostException, IOException {
		client = new SocketClientTest();
		client.link();
	}

	private void link() throws IOException {
		socket = new Socket(host, port);
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
	}

	/**
	 * 测试注册功能
	 * 
	 * @author Feng
	 * @throws IOException 
	 */
	@Test
	public void testRegister() throws IOException {
		String randomData = (((int) (Math.random() * 100000)) + "").substring(0, 5);
		byte[] resultBytes = client.testRegister_JUint(randomData, randomData, randomData);
		RegisterMsg.RegisterRsp responseObject = RegisterMsg.RegisterRsp.parseFrom(NetworkMessage
				.getMessageObjectBytes(resultBytes));

		assertEquals(responseObject.getResultCode().toString(), RegisterMsg.RegisterRsp.ResultCode.SUCCESS.toString());

		resultBytes = client.testRegister_JUint(randomData, randomData, randomData);
		responseObject = RegisterMsg.RegisterRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), RegisterMsg.RegisterRsp.ResultCode.USER_EXIST.toString());
		// assertEquals(responseObject.getResultCode().toString(),
		// RegisterMsg.RegisterRsp.ResultCode.SUCCESS.toString());
		// String resutString = resultBytes
	}

	/**
	 * 测试登陆功能
	 * 
	 * @author Feng
	 * @throws IOException
	 *             s
	 * @throws UnknownHostException
	 */
	@Test
	public void testLogin() throws UnknownHostException, IOException {
		byte[] resultBytes = client.testLogin_JUint("a", "aa");
		LoginMsg.LoginRsp responseObject = LoginMsg.LoginRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));

		assertEquals(responseObject.getResultCode().toString(), LoginMsg.LoginRsp.ResultCode.SUCCESS);

		resultBytes = client.testLogin_JUint("aa", "aa");
		assertEquals(responseObject.getResultCode().toString(), LoginMsg.LoginRsp.ResultCode.FAIL);

		resultBytes = client.testLogin_JUint("a", "aaa");
		assertEquals(responseObject.getResultCode().toString(), LoginMsg.LoginRsp.ResultCode.FAIL);
	}

}
