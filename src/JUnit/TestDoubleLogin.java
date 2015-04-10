package JUnit;

import static org.junit.Assert.*;
import java.io.IOException;
import java.net.UnknownHostException;
import org.junit.Before;
import org.junit.Test;
import protocol.ProtoHead;
import protocol.Msg.LoginMsg;
import protocol.Msg.LoginMsg.LoginRsp.ResultCode;
import server.NetworkPacket;

import client.SocketClientTest;

/**
 * 测试第二个人登陆后第一个人被踢下来的情况
 * 
 * @author Feng
 * 
 */
public class TestDoubleLogin {
//	public SocketClientTest client1, client2;

//	@Before
//	public void init() throws UnknownHostException, IOException {
//		client1 = new SocketClientTest();
//		client1.link();
//		client2 = new SocketClientTest();
//		client2.link();
//	}

	@Test
	public void test() throws UnknownHostException, IOException {
		ClientSocket client1, client2;
		String user = "a";
		
		// 1号客户端登陆
		client1 = new ClientSocket();
		ResultCode resultCode = client1.login(user, user);
		assertEquals(resultCode, LoginMsg.LoginRsp.ResultCode.SUCCESS);

		// 2号客户端登陆
		client2 = new ClientSocket();
		resultCode = client2.login(user, user);
		assertEquals(resultCode, LoginMsg.LoginRsp.ResultCode.SUCCESS);

		// 检测1号客户端的收到的“踢下线”消息
		byte[] resultBytes;
		boolean getResponse = false;
		for (int i = 0; i < 2; i++) {
			resultBytes = client1.readFromServerWithoutKeepAlive();
			// 其他消息，不管
			if (ProtoHead.ENetworkMessage.OFFLINE_SYNC != NetworkPacket.getMessageType(resultBytes))
				continue;
			// System.err.println(NetworkMessage.getMessageType(resultBytes));

			// 回复服务器
//			client1.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.OFFLINE_SYNC_VALUE,
//					NetworkPacket.getMessageID(resultBytes), new byte[]{}));

			// 踢人通知
			assertTrue(true);
			getResponse = true;
			return;
		}
		if (!getResponse)
			assertFalse(true);
	}
}
