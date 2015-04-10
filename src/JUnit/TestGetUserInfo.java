package JUnit;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import org.junit.Before;
import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.GetUserInfoMsg;
import server.NetworkPacket;
import tools.DataTypeTranslater;

import client.SocketClientTest;

/**
 * 对获取用户信息的测试
 * @author wangfei
 *
 */
public class TestGetUserInfo {
	private String user = "a";
//	String host = "192.168.45.17"; // 要连接的服务端IP地址
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
	 * 测试获取用户信息--JUnit调用
	 * 
	 * @param targetUserId
	 * @return
	 * @throws IOException
	 * @author wangfei
	 */
	public GetUserInfoMsg.GetUserInfoRsp testGetUserInfo_JUnit(ClientSocket client, String targetUserId) throws IOException {
		GetUserInfoMsg.GetUserInfoReq.Builder builder = GetUserInfoMsg.GetUserInfoReq.newBuilder();
		builder.setTargetUserId(targetUserId);
		byte[] byteArray = NetworkPacket.packMessage(ProtoHead.ENetworkMessage.GET_USERINFO_REQ.getNumber(), builder.build()
				.toByteArray());
		client.writeToServer(byteArray);
		
		for (int i=0; i<10; i++) {
			byteArray = client.readFromServerWithoutKeepAlive();
			ProtoHead.ENetworkMessage type = ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(byteArray,
					NetworkPacket.getTypeStartIndex()));
			if (type == ProtoHead.ENetworkMessage.GET_USERINFO_RSP) {
				return GetUserInfoMsg.GetUserInfoRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
			}
		}
		return null;
	}
	
	/**
	 * 测获取用户信息
	 * @author wangfei
	 * @throws IOException
	 */
	@Test
	public void testGetUserInfo() throws IOException{
		ClientSocket client = new ClientSocket();
		
		GetUserInfoMsg.GetUserInfoRsp responseObject = testGetUserInfo_JUnit(client, user);
		assertEquals(responseObject.getResultCode().toString(), GetUserInfoMsg.GetUserInfoRsp.ResultCode.SUCCESS.toString());

		responseObject = testGetUserInfo_JUnit(client, user);
		assertEquals(responseObject.getResultCode().toString(), GetUserInfoMsg.GetUserInfoRsp.ResultCode.FAIL.toString());
	}

}
