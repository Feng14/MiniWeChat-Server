package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.CreateGroupChatMsg.CreateGroupChatReq;
import protocol.Msg.CreateGroupChatMsg.CreateGroupChatRsp;
import protocol.Msg.LoginMsg.LoginRsp;
import server.NetworkPacket;

public class TestCreateGroupChatting {

	private CreateGroupChatRsp createGroupChatting(ClientSocket clientSocket, String[] userList) throws IOException {
		CreateGroupChatReq.Builder builder = CreateGroupChatReq.newBuilder();
		for (String s : userList)
			builder.addUserId(s);

		clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CREATE_GROUP_CHAT_REQ_VALUE, builder.build()
				.toByteArray()));
		
		byte[] arrayBytes; 
		for (int i=0; i<10; i++) {
			arrayBytes = clientSocket.readFromServerWithoutKeepAlive();
			
			if (NetworkPacket.getMessageType(arrayBytes) != ProtoHead.ENetworkMessage.CREATE_GROUP_CHAT_RSP)
				continue;
			
			return CreateGroupChatRsp.parseFrom(NetworkPacket.getMessageObjectBytes(arrayBytes));
		}
		return null;
	}

	/**
	 * 测试正常情况
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	@Test
	public void test() throws UnknownHostException, IOException {
		String userId = "a";
		ClientSocket clientSocket = new ClientSocket();

		LoginRsp.ResultCode resultCode = clientSocket.login(userId, userId);
		assertEquals(resultCode, LoginRsp.ResultCode.SUCCESS);
		System.out.println(resultCode);
		
		CreateGroupChatRsp response = createGroupChatting(clientSocket, new String[]{"a", "b", "c"});
		assertEquals(response.getResultCode(), CreateGroupChatRsp.ResultCode.SUCCESS);
		System.out.println("GroupId : " + response.getGroupChatId());
	}

}
