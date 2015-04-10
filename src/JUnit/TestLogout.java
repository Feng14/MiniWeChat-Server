package JUnit;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import org.junit.Before;
import org.junit.Test;
import protocol.Msg.LogoutMsg;
import protocol.Msg.LoginMsg.LoginRsp;
import protocol.Msg.LogoutMsg.LogoutRsp;
import server.NetworkPacket;
import client.SocketClientTest;

public class TestLogout {
	private String user = "a";
//	String host = "192.168.45.34"; // 要连接的服务端IP地址
//	int port = 8080; // 要连接的服务端对应的监听端口

//	public Socket socket;
//	public InputStream inputStream;
//	public OutputStream outputStream;
//	public SocketClientTest client;

//	@Before
//	public void init() throws UnknownHostException, IOException {
//		client = new SocketClientTest();
//		client.link();
//	}
//
//	private void link() throws IOException {
//		socket = new Socket(host, port);
//		inputStream = socket.getInputStream();
//		outputStream = socket.getOutputStream();
//	}

	/**
	 * 测试退出登录
	 * 
	 * @throws IOException
	 */
	@Test
	public void testLogout() throws IOException {
		ClientSocket client = new ClientSocket();
		
		LoginRsp.ResultCode resultCode = client.login(user, user);
		assertEquals(resultCode, LoginRsp.ResultCode.SUCCESS);
//		byte[] resultBytes = client.testLogout_JUnit();
//		LogoutMsg.LogoutRsp responseObject = LogoutMsg.LogoutRsp.parseFrom(NetworkPacket.getMessageObjectBytes(resultBytes));
//		assertEquals(responseObject.getResultCode().toString(), LogoutMsg.LogoutRsp.ResultCode.SUCCESS.toString());

		LogoutRsp.ResultCode resultCode2 = client.logout();
		assertEquals(resultCode2, LogoutRsp.ResultCode.SUCCESS);
//		resultBytes = client.testLogout_JUnit();
//		responseObject = LogoutMsg.LogoutRsp.parseFrom(NetworkPacket.getMessageObjectBytes(resultBytes));
//		assertEquals(responseObject.getResultCode().toString(), LogoutMsg.LogoutRsp.ResultCode.FAIL.toString());
	}

}
