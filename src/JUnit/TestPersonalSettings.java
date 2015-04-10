package JUnit;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.PersonalSettingsMsg;
import protocol.Msg.PersonalSettingsMsg.PersonalSettingsRsp;
import server.NetworkPacket;
import tools.DataTypeTranslater;


/**
 * 对个人设置功能的测试
 * 
 * @author wangfei
 * 
 */
public class TestPersonalSettings {
	// String host = "192.168.45.17"; // 要连接的服务端IP地址
	// int port = 8080; // 要连接的服务端对应的监听端口
	//
	// public Socket socket;
	// public InputStream inputStream;
	// public OutputStream outputStream;
	// public SocketClientTest client;
	//
	// @Before
	// public void init() throws UnknownHostException, IOException {
	// client = new SocketClientTest();
	// client.link();
	// }
	//
	// private void link() throws IOException {
	// socket = new Socket(host, port);
	// inputStream = socket.getInputStream();
	// outputStream = socket.getOutputStream();
	// }

	/**
	 * 测试个人设置--JUnit调用
	 * 
	 * @param userName
	 * @param userPassword
	 * @return
	 * @throws IOException
	 * @author wangfei
	 */
	public PersonalSettingsRsp.ResultCode testPersonalSettings_JUnit(ClientSocket client, String userName, String userPassword,
			int headIndex) throws IOException {
		PersonalSettingsMsg.PersonalSettingsReq.Builder builder = PersonalSettingsMsg.PersonalSettingsReq.newBuilder();
		builder.setUserName(userName);
		builder.setUserPassword(userPassword);
		builder.setHeadIndex(headIndex);
		byte[] byteArray = NetworkPacket.packMessage(ProtoHead.ENetworkMessage.PERSONALSETTINGS_REQ.getNumber(), builder.build()
				.toByteArray());
		client.writeToServer(byteArray);

		for (int i = 0; i < 10; i++) {
			byteArray = client.readFromServerWithoutKeepAlive();
			ProtoHead.ENetworkMessage type = ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(byteArray,
					NetworkPacket.getTypeStartIndex()));
			if (type == ProtoHead.ENetworkMessage.PERSONALSETTINGS_RSP) {
				return PersonalSettingsRsp.parseFrom(byteArray).getResultCode();
			}
		}
		return PersonalSettingsRsp.ResultCode.FAIL;
	}

	/**
	 * 测试个人设置
	 * 
	 * @author wangfei
	 * @throws IOException
	 */
	@Test
	public void testPersonalSettings() throws IOException {
		ClientSocket client = new ClientSocket();

		String randomData = (((int) (Math.random() * 100000)) + "").substring(0, 5);
		int randomIndex = (int) Math.random() * 6;

		PersonalSettingsRsp.ResultCode resultCode = testPersonalSettings_JUnit(client, randomData, randomData, randomIndex);
		assertEquals(resultCode, PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.SUCCESS);

		resultCode = testPersonalSettings_JUnit(client, randomData, randomData, randomIndex);
		assertEquals(resultCode, PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
	}
}
