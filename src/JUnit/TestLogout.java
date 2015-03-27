package JUnit;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import protocol.Msg.DeleteFriendMsg;
import protocol.Msg.LogoutMsg;
import server.NetworkMessage;

import client.SocketClientTest;

public class TestLogout {
	String host = "192.168.45.34"; // 要连接的服务端IP地址
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
	 * 测试退出登录
	 * 
	 * @throws IOException
	 */
	@Test
	public void testLogout() throws IOException {
		byte[] resultBytes = client.testLogout_JUnit();
		LogoutMsg.LogoutRsp responseObject = LogoutMsg.LogoutRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), LogoutMsg.LogoutRsp.ResultCode.SUCCESS.toString());

		resultBytes = client.testLogout_JUnit();
		responseObject = LogoutMsg.LogoutRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), LogoutMsg.LogoutRsp.ResultCode.FAIL.toString());
	}

}
