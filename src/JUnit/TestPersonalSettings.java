package JUnit;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import org.junit.Before;
import org.junit.Test;
import protocol.Msg.PersonalSettingsMsg;
import server.NetworkMessage;

import client.SocketClientTest;

/**
 * 对个人设置功能的测试
 * @author wangfei
 *
 */
public class TestPersonalSettings {
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
	 * 测试个人设置
	 * @author wangfei
	 * @throws IOException
	 */
	@Test
	public void testPersonalSettings() throws IOException{
		
		String randomData = (((int) (Math.random() * 100000)) + "").substring(0, 5);
		int randomIndex =(int)Math.random()*6;
		
		byte[] resultBytes = client.testPersonalSettings_JUnit(randomData, randomData,randomIndex);
		PersonalSettingsMsg.PersonalSettingsRsp responseObject = 
				PersonalSettingsMsg.PersonalSettingsRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(),PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.SUCCESS.toString());

		resultBytes = client.testPersonalSettings_JUnit(randomData, randomData,randomIndex);
		responseObject =PersonalSettingsMsg.PersonalSettingsRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL.toString());
	}
}
