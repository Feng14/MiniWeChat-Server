package JUnit;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Before;

import protocol.Msg.GetUserInfoMsg;
import protocol.Msg.PersonalSettingsMsg;
import protocol.Msg.RegisterMsg;
import server.NetworkMessage;

import client.SocketClientTest;

/**
 * 对获取用户信息的测试
 * @author wangfei
 *
 */
public class TestGetUserInfo {
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
	 * 测获取用户信息
	 * @author wangfei
	 * @throws IOException
	 */
	public void testGetUserInfo() throws IOException{
		String randomData = (((int) (Math.random() * 100000)) + "").substring(0, 5);
		
		byte[] resultBytes = client.testGetUserInfo_JUnit(randomData);
		GetUserInfoMsg.GetUserInfoRsp responseObject = 
				GetUserInfoMsg.GetUserInfoRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), GetUserInfoMsg.GetUserInfoRsp.ResultCode.SUCCESS.toString());

		resultBytes = client.testGetUserInfo_JUnit(randomData);
		responseObject =GetUserInfoMsg.GetUserInfoRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), GetUserInfoMsg.GetUserInfoRsp.ResultCode.FAIL.toString());
	}

}
