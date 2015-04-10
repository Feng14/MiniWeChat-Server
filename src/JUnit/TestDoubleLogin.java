package JUnit;

import static org.junit.Assert.*;
import java.io.IOException;
import java.net.UnknownHostException;
import org.junit.Before;
import org.junit.Test;
import protocol.ProtoHead;
import protocol.Msg.LoginMsg;
import server.NetworkPacket;

import client.SocketClientTest;

/**
 * 测试第二个人登陆后第一个人被踢下来的情况
 * 
 * @author Feng
 * 
 */
public class TestDoubleLogin {
	public SocketClientTest client1, client2;

	@Before
	public void init() throws UnknownHostException, IOException {
		client1 = new SocketClientTest();
		client1.link();
		client2 = new SocketClientTest();
		client2.link();
	}

	@Test
	public void test() throws UnknownHostException, IOException {
		// 1号客户端登陆
		byte[] resultBytes = client1.testLogin_JUint("a", "aa");
		LoginMsg.LoginRsp responseObject = LoginMsg.LoginRsp.parseFrom(NetworkPacket.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), LoginMsg.LoginRsp.ResultCode.SUCCESS.toString());

		// 2号客户端登陆
		resultBytes = client2.testLogin_JUint("a", "aa");
		responseObject = LoginMsg.LoginRsp.parseFrom(NetworkPacket.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), LoginMsg.LoginRsp.ResultCode.SUCCESS.toString());

		// 检测1号客户端的收到的“踢下线”消息
		for (int i = 0; i < 2; i++) {
			resultBytes = client1.readFromServer();
			// 其他消息，不管
			if (ProtoHead.ENetworkMessage.OFFLINE_SYNC != NetworkPacket.getMessageType(resultBytes))
				continue;
			// System.err.println(NetworkMessage.getMessageType(resultBytes));

			// 回复服务器
			client1.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.OFFLINE_SYNC_VALUE,
					NetworkPacket.getMessageID(resultBytes), new byte[]{}));

			// 踢人通知
			assertTrue(true);
			return;
		}
		assertFalse(true);
	}
}
