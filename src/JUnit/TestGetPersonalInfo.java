package JUnit;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import org.junit.Before;
import protocol.Msg.GetPersonalInfoMsg;
import protocol.Msg.GetUserInfoMsg;
import server.NetworkMessage;

import client.SocketClientTest;

public class TestGetPersonalInfo {
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
	 * 测获取个人信息
	 * @throws IOException
	 * @author wangfei 
	 * @time 2015-03-26
	 */
	public void testGetPersonalInfo() throws IOException{
		Random random = new Random();
		boolean userInfo = random.nextBoolean();
		boolean friendInfo = random.nextBoolean();
		
		byte[] resultBytes = client.testGetPersonalInfo_JUnit(userInfo,friendInfo);
		GetPersonalInfoMsg.GetPersonalInfoRsp responseObject = 
				GetPersonalInfoMsg.GetPersonalInfoRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), GetUserInfoMsg.GetUserInfoRsp.ResultCode.SUCCESS.toString());

		resultBytes = client.testGetPersonalInfo_JUnit(userInfo,friendInfo);
		responseObject =GetPersonalInfoMsg.GetPersonalInfoRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), GetPersonalInfoMsg.GetPersonalInfoRsp.ResultCode.FAIL.toString());
	}

}
