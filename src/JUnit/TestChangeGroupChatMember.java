package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.ChangeGroupChatMemberMsg.ChangeGroupChatMemberRsp;
import protocol.Msg.ChangeGroupChatMemberMsg.ChangeGroupChatMemberRsq;
import protocol.Msg.ChangeGroupChatMemberMsg.ChangeGroupChatMemberSync;
import protocol.Msg.ChangeGroupChatMemberMsg.ChangeGroupChatMemberRsq.ChangeType;
import protocol.Msg.LoginMsg.LoginRsp;
import protocol.Msg.LogoutMsg.LogoutRsp;
import server.NetworkPacket;

/**
 * 对“修改群聊用户”功能的测试
 * 
 * @author Feng
 * 
 */
public class TestChangeGroupChatMember {

	/**
	 * 测试添加新用户
	 * 
	 * @author Feng
	 * @throws IOException
	 * @throws UnknownHostException
	 */
//	@Test
	public void test1() throws UnknownHostException, IOException {
		int groupId = 13;
		ClientSocket clientSocket1 = new ClientSocket();
		ClientSocket clientSocket2 = new ClientSocket();

		ChangeGroupChatMemberRsq.Builder builder = ChangeGroupChatMemberRsq.newBuilder();
		builder.setChangeType(ChangeType.ADD);
		builder.setGroupId(groupId);
		builder.addUserId("d");
		builder.addUserId("e");

		// 无权限添加
		clientSocket1.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER_REQ_VALUE, builder
				.build().toByteArray()));
		
		byte[] byteArray = clientSocket1.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER__RSP);
		assertNotNull(byteArray);
		ChangeGroupChatMemberRsp response = ChangeGroupChatMemberRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(response.getResultCode(), ChangeGroupChatMemberRsp.ResultCode.NO_AUTHORITY);
//		System.out.println(response.getResultCode().toString());
		
		// 登陆后，有权限添加
		assertEquals(clientSocket1.login("b", "b"), LoginRsp.ResultCode.SUCCESS);
		assertEquals(clientSocket2.login("c", "c"), LoginRsp.ResultCode.SUCCESS);
		
		clientSocket1.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER_REQ_VALUE, builder
				.build().toByteArray()));
		
		byteArray = clientSocket1.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER__RSP);
		assertNotNull(byteArray);
		response = ChangeGroupChatMemberRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(response.getResultCode(), ChangeGroupChatMemberRsp.ResultCode.SUCCESS);
		
		// user2上线收消息
		byteArray = clientSocket2.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER__Sync);
		assertNotNull(byteArray);
		ChangeGroupChatMemberSync sync = ChangeGroupChatMemberSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		
		
		clientSocket1.close();
		clientSocket2.close();
	}

	/**
	 * 测试删除群聊成员
	 * @throws UnknownHostException
	 * @throws IOException
	 * @author Feng
	 */
	@Test
	public void test2() throws UnknownHostException, IOException{
		int groupId = 13;
		String user1 = "e", user2 = "a";
		ClientSocket clientSocket1 = new ClientSocket();
		ClientSocket clientSocket2 = new ClientSocket();

		ChangeGroupChatMemberRsq.Builder builder = ChangeGroupChatMemberRsq.newBuilder();
		builder.setChangeType(ChangeType.DELETE);
		builder.setGroupId(groupId);
//		builder.addUserId("d");
//		builder.addUserId("e");

		clientSocket1.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER_REQ_VALUE, builder
				.build().toByteArray()));
		
		// 未登录，无权限
		byte[] byteArray = clientSocket1.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER__RSP);
		assertNotNull(byteArray);
		ChangeGroupChatMemberRsp response = ChangeGroupChatMemberRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(response.getResultCode(), ChangeGroupChatMemberRsp.ResultCode.NO_AUTHORITY);
		System.out.println(response.getResultCode().toString());
		
		// 删除自己，成功
		assertEquals(clientSocket1.login(user1, user1), LoginRsp.ResultCode.SUCCESS);
		System.out.println(user1 + " login");
		assertEquals(clientSocket2.login(user2, user2), LoginRsp.ResultCode.SUCCESS);
		
		clientSocket1.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER_REQ_VALUE, builder
				.build().toByteArray()));
		byteArray = clientSocket1.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER__RSP);
		assertNotNull(byteArray);
		response = ChangeGroupChatMemberRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(response.getResultCode(), ChangeGroupChatMemberRsp.ResultCode.SUCCESS);

		// user2上线收消息
		byteArray = clientSocket2.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER__Sync);
		assertNotNull(byteArray);
		ChangeGroupChatMemberSync sync = ChangeGroupChatMemberSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		
		clientSocket1.close();
		clientSocket2.close();
	}
}
