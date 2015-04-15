package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.ChangeGroupChatMemberMsg.ChangeGroupChatMemberRsp;
import protocol.Msg.ChangeGroupChatMemberMsg.ChangeGroupChatMemberRsq;
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
		ClientSocket clientSocket = new ClientSocket();

		ChangeGroupChatMemberRsq.Builder builder = ChangeGroupChatMemberRsq.newBuilder();
		builder.setChangeType(ChangeType.ADD);
		builder.setGroupId(groupId);
		builder.addUserId("d");
		builder.addUserId("e");

		// 无权限添加
		clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER_REQ_VALUE, builder
				.build().toByteArray()));
		
		byte[] byteArray = clientSocket.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER__RSP);
		assertNotNull(byteArray);
		ChangeGroupChatMemberRsp response = ChangeGroupChatMemberRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(response.getResultCode(), ChangeGroupChatMemberRsp.ResultCode.NO_AUTHORITY);
//		System.out.println(response.getResultCode().toString());
		
		// 登陆后，有权限添加
		clientSocket.login("b", "b");
		clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER_REQ_VALUE, builder
				.build().toByteArray()));
		
		byteArray = clientSocket.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER__RSP);
		assertNotNull(byteArray);
		response = ChangeGroupChatMemberRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(response.getResultCode(), ChangeGroupChatMemberRsp.ResultCode.SUCCESS);
		
		clientSocket.close();
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
		ClientSocket clientSocket = new ClientSocket();

		ChangeGroupChatMemberRsq.Builder builder = ChangeGroupChatMemberRsq.newBuilder();
		builder.setChangeType(ChangeType.DELETE);
		builder.setGroupId(groupId);
		builder.addUserId("d");
		builder.addUserId("e");

		clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER_REQ_VALUE, builder
				.build().toByteArray()));
		
		// 未登录，无权限
		byte[] byteArray = clientSocket.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER__RSP);
		assertNotNull(byteArray);
		ChangeGroupChatMemberRsp response = ChangeGroupChatMemberRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(response.getResultCode(), ChangeGroupChatMemberRsp.ResultCode.NO_AUTHORITY);
		System.out.println(response.getResultCode().toString());
		
		// 非创建者，无权限
		assertEquals(clientSocket.login("b", "b"), LoginRsp.ResultCode.SUCCESS);
		System.out.println("b login");
		
		clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER_REQ_VALUE, builder
				.build().toByteArray()));
		byteArray = clientSocket.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER__RSP);
		assertNotNull(byteArray);
		response = ChangeGroupChatMemberRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(response.getResultCode(), ChangeGroupChatMemberRsp.ResultCode.NO_AUTHORITY);
		System.out.println(response.getResultCode().toString());
		
		assertEquals(clientSocket.logout(), LogoutRsp.ResultCode.SUCCESS);
		System.out.println("b logout");
		
		// 创建者，成功
		assertEquals(clientSocket.login("a", "a"), LoginRsp.ResultCode.SUCCESS);
		System.out.println("a login");
		
		clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER_REQ_VALUE, builder
				.build().toByteArray()));
		byteArray = clientSocket.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER__RSP);
		assertNotNull(byteArray);
		response = ChangeGroupChatMemberRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(response.getResultCode(), ChangeGroupChatMemberRsp.ResultCode.SUCCESS);
	}
}
