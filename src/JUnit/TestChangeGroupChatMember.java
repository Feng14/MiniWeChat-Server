package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.ChangeGroupChatMemberMsg.ChangeGroupChatMemberRsp;
import protocol.Msg.ChangeGroupChatMemberMsg.ChangeGroupChatMemberRsq;
import protocol.Msg.ChangeGroupChatMemberMsg.ChangeGroupChatMemberRsq.ChangeType;
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

		clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER_REQ_VALUE, builder
				.build().toByteArray()));
		
		byte[] byteArray = clientSocket.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER__RSP);
		assertNotNull(byteArray);
		ChangeGroupChatMemberRsp response = ChangeGroupChatMemberRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(response.getResultCode(), ChangeGroupChatMemberRsp.ResultCode.SUCCESS);
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
		
		byte[] byteArray = clientSocket.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER__RSP);
		assertNotNull(byteArray);
		ChangeGroupChatMemberRsp response = ChangeGroupChatMemberRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(response.getResultCode(), ChangeGroupChatMemberRsp.ResultCode.SUCCESS);
		
	}
}
