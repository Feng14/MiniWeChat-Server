package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;

import protocol.ProtoHead;
import protocol.Data.GroupData.GroupItem;
import protocol.Msg.ChangeGroupMsg.ChangeGroupSync;
import protocol.Msg.CreateGroupChatMsg.CreateGroupChatReq;
import protocol.Msg.CreateGroupChatMsg.CreateGroupChatRsp;
import protocol.Msg.LoginMsg.LoginRsp;
import protocol.Msg.ReceiveChatMsg.ReceiveChatSync;
import server.NetworkPacket;

public class TestCreateGroupChatting {

	private CreateGroupChatRsp createGroupChatting(ClientSocket clientSocket, String[] userList) throws IOException {
		CreateGroupChatReq.Builder builder = CreateGroupChatReq.newBuilder();
		for (String s : userList)
			builder.addUserId(s);

		clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CREATE_GROUP_CHAT_REQ_VALUE, builder
				.build().toByteArray()));

		byte[] arrayBytes;
		for (int i = 0; i < 10; i++) {
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
		System.out.println(user1 + " login");
		LoginRsp.ResultCode resultCode = clientSocket1.login(user1, user1);
		assertEquals(resultCode, LoginRsp.ResultCode.SUCCESS);
		System.out.println(resultCode);

		// user2登陆
		System.out.println(user2 + " login");
		assertEquals(clientSocket2.login(user2, user2), LoginRsp.ResultCode.SUCCESS);

		// 创建成功？
		System.out.println(user1 + " create Group");
		CreateGroupChatRsp response = createGroupChatting(clientSocket1, new String[] { "b" });
		assertEquals(response.getResultCode(), CreateGroupChatRsp.ResultCode.SUCCESS);
		System.out.println("GroupId : " + response.getGroupChatId());

		// user2接收到有新群消息
		System.out.println(user2 + " Wait Message");
		byte[] byteArray;
		for (int i = 0; i < 2; i++) {
			byteArray = clientSocket2.readFromServerWithoutKeepAlive();
			System.out.println("Receive one Message!");
			if (NetworkPacket.getMessageType(byteArray) == ProtoHead.ENetworkMessage.CHANGE_GROUP_SYNC) {
				// 成员变化通知
				System.out.println("成员变化通知");
				ChangeGroupSync changeGroupSync = ChangeGroupSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
				GroupItem groupItem = changeGroupSync.getGroupItem();
				assertEquals(groupItem.getCreaterUserId(), user1);
				assertEquals(groupItem.getMemberUserIdCount(), 2);
			} else if (NetworkPacket.getMessageType(byteArray) == ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC) {
				// 收到群聊消息（A 我已经把 B，C 拉入群聊）
				System.out.println(user2 + "receive : "
						+ ReceiveChatSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray)).getChatData(0).getChatBody());
			}
		}

		clientSocket1.close();
		clientSocket2.close();
	}
}
