package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.ChangeGroupChatMemberMsg.ChangeGroupChatMemberSync;
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
		String user1 = "a", user2 = "b";
		ClientSocket clientSocket1 = new ClientSocket();
		ClientSocket clientSocket2 = new ClientSocket();
		

		// user1 登陆
		LoginRsp.ResultCode resultCode = clientSocket1.login(user1, user1);
		assertEquals(resultCode, LoginRsp.ResultCode.SUCCESS);
		System.out.println(resultCode);
		
		// user2登陆
		assertEquals(clientSocket2.login(user2, user2), LoginRsp.ResultCode.SUCCESS);
		
		// 创建成功？
		CreateGroupChatRsp response = createGroupChatting(clientSocket1, new String[]{"a", "b"});
		assertEquals(response.getResultCode(), CreateGroupChatRsp.ResultCode.SUCCESS);
		System.out.println("GroupId : " + response.getGroupChatId());
		
		// user2接收到有新群消息
		byte[] byteArray = clientSocket2.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER__Sync);
		assertNotNull(byteArray);
		ChangeGroupChatMemberSync sync = ChangeGroupChatMemberSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
//		System.out.println("Get Sycn");
		System.out.println(sync.toString());
		
		clientSocket1.close();
		clientSocket2.close();
	}

}
