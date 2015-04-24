package JUnit;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import org.junit.Before;
import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.RegisterMsg;
import protocol.Msg.PersonalSettingsMsg.PersonalSettingsRsp;
import protocol.Msg.RegisterMsg.RegisterRsp;
import server.NetworkPacket;

import client.SocketClientTest;

/**
 * 对注册功能的测试（要先开服务器）
 * @author Feng
 *
 */
public class TestRegister {
	// String host = "192.168.45.11"; // 要连接的服务端IP地址
//	String host = "192.168.45.34"; // 要连接的服务端IP地址
//	int port = 8080; // 要连接的服务端对应的监听端口
//
//	public Socket socket;
//	public InputStream inputStream;
//	public OutputStream outputStream;
//	public SocketClientTest client;
//
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
	 * 测试注册功能(由JUnit调用)
	 * 
	 * @author Feng
	 * @return
	 * @throws IOException
	 */
	private RegisterRsp.ResultCode testRegister_JUint(ClientSocket client, String userId, String userPassword, String userName) throws IOException {
		RegisterMsg.RegisterReq.Builder builder = RegisterMsg.RegisterReq.newBuilder();
		builder.setUserId(userId);
		builder.setUserPassword(userPassword);
		builder.setUserName(userName);

		byte[] byteArray = NetworkPacket.packMessage(ProtoHead.ENetworkMessage.REGISTER_REQ.getNumber(), builder.build()
				.toByteArray());
		client.writeToServer(byteArray);
		
		for (int i=0; i<10; i++) {
			byteArray = client.readFromServerWithoutKeepAlive();
			System.out.println(NetworkPacket.getMessageType(byteArray).toString());
			if (NetworkPacket.getMessageType(byteArray) != ProtoHead.ENetworkMessage.REGISTER_RSP)
				continue;

			return RegisterRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray)).getResultCode();
		}
		return RegisterRsp.ResultCode.USER_EXIST;
	}

	/**
	 * 测试注册功能
	 * 
	 * @author Feng
	 * @throws IOException
	 */
	@Test
	public void testRegister() throws IOException {
		ClientSocket client = new ClientSocket();
		
//		String randomData = (((int) (Math.random() * 100000)) + "").substring(0, 5);
		String randomData = "g";
		RegisterRsp.ResultCode resultCode = testRegister_JUint(client, randomData, randomData, randomData);
		assertEquals(RegisterMsg.RegisterRsp.ResultCode.SUCCESS, resultCode);

		resultCode = testRegister_JUint(client, randomData, randomData, randomData);
		assertEquals(RegisterMsg.RegisterRsp.ResultCode.USER_EXIST, resultCode);
		
		client.close();
	}
}
