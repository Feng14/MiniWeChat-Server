package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;

import protocol.ProtoHead;
import protocol.Data.ChatData.ChatItem;
import protocol.Data.GroupData.GroupItem;
import protocol.Msg.ChangeGroupMsg.ChangeGroupReq;
import protocol.Msg.ChangeGroupMsg.ChangeGroupReq.ChangeType;
import protocol.Msg.ChangeGroupMsg.ChangeGroupRsp;
import protocol.Msg.ChangeGroupMsg.ChangeGroupSync;
import protocol.Msg.LoginMsg.LoginRsp;
import protocol.Msg.LogoutMsg.LogoutRsp;
import protocol.Msg.ReceiveChatMsg.ReceiveChatSync;
import server.NetworkPacket;
import tools.DataTypeTranslater;

/**
 * 对“修改群聊用户”功能的测试
 * 
 * @author Feng
 * 
 */
public class TestChangeGroup {

	private ChangeGroupRsp sendRequest(ClientSocket clientSocket, ChangeGroupReq.Builder builder) throws IOException {
		clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_REQ_VALUE, builder.build()
				.toByteArray()));

		byte[] byteArray = clientSocket.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_RSP);
		return ChangeGroupRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
	}

	/**
	 * 测试添加新用户(无权限)
	 * 
	 * @author Feng
	 * @throws IOException
	 * @throws UnknownHostException
	 */
//	 @Test
	public void addMember1() throws UnknownHostException, IOException {
		String groupId = "13";
		ClientSocket clientSocket1 = new ClientSocket();

		ChangeGroupReq.Builder builder = ChangeGroupReq.newBuilder();
		builder.setChangeType(ChangeType.ADD);
		builder.setGroupId(groupId);
		builder.addUserId("d");
		builder.addUserId("e");

		// 无权限添加
		ChangeGroupRsp response = sendRequest(clientSocket1, builder);
		assertEquals(response.getResultCode(), ChangeGroupRsp.ResultCode.NO_AUTHORITY);

		clientSocket1.close();
	}

	/**
	 * 测试添加新用户(有权限，成功)
	 * 
	 * @author Feng
	 * @throws IOException
	 * @throws UnknownHostException
	 */
//	@Test
	public void addMember2() throws UnknownHostException, IOException {
		String groupId = "13", user1 = "b", user2 = "c";
		ClientSocket clientSocket1 = new ClientSocket();
		ClientSocket clientSocket2 = new ClientSocket();

		ChangeGroupReq.Builder builder = ChangeGroupReq.newBuilder();
		builder.setChangeType(ChangeType.ADD);
		builder.setGroupId(groupId);
		builder.addUserId("d");
		builder.addUserId("e");

		// 登陆
		assertEquals(clientSocket1.login(user1, user1), LoginRsp.ResultCode.SUCCESS);
		assertEquals(clientSocket2.login(user2, user2), LoginRsp.ResultCode.SUCCESS);
		System.out.println(user1 + " , " + user2 + " Login Over!");

		// 发送增加成员请求
		ChangeGroupRsp response = sendRequest(clientSocket1, builder);
		assertEquals(response.getResultCode(), ChangeGroupRsp.ResultCode.SUCCESS);

		// user1 接收同步数据（CHANGE_GROUP_SYNC）包
		byte[] byteArray = clientSocket1.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_SYNC);
		assertNotNull(byteArray);
		ChangeGroupSync sync = ChangeGroupSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		GroupItem groupItem = sync.getGroupItem();
		// log
		System.out.println("CHANGE_GROUP_SYNC : " + user1 + " : " + ClientSocket.getGroupItemInfo(groupItem));

		// user2上线收消息
		// 收取群数据更新
		byteArray = clientSocket2.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_SYNC);
		assertNotNull(byteArray);
		sync = ChangeGroupSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		groupItem = sync.getGroupItem();
		// log
		System.out.println("CHANGE_GROUP_SYNC : " + user2 + " : " + ClientSocket.getGroupItemInfo(groupItem));

		byteArray = clientSocket2.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC);
		ReceiveChatSync receiveChatting = ReceiveChatSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		ChatItem chatItem2 = receiveChatting.getChatData(0);
		
		// 收到群聊消息（A 我已经把 B，C 拉入群聊）
		System.out.println("RECEIVE_CHAT_SYNC : " + user2 + " : " + ClientSocket.getChatItemInfo(chatItem2));

		clientSocket1.close();
		clientSocket2.close();
	}

	/**
	 * 测试删除群聊成员(无权限
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @author Feng
	 */
	// @Test
	public void deleteMember1() throws UnknownHostException, IOException {
		String groupId = "13";
		ClientSocket clientSocket1 = new ClientSocket();

		ChangeGroupReq.Builder builder = ChangeGroupReq.newBuilder();
		builder.setChangeType(ChangeType.DELETE);
		builder.setGroupId(groupId);
		// builder.addUserId("d");
		// builder.addUserId("e");

		clientSocket1.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_REQ_VALUE, builder.build()
				.toByteArray()));

		// 未登录，无权限
		ChangeGroupRsp response = sendRequest(clientSocket1, builder);
		assertEquals(response.getResultCode(), ChangeGroupRsp.ResultCode.NO_AUTHORITY);
		System.out.println(response.getResultCode().toString());

		clientSocket1.close();
	}

	/**
	 * 测试删除群聊成员
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @author Feng
	 */
//	 @Test
	public void deleteMember2() throws UnknownHostException, IOException {
		String groupId = "13";
		String user1 = "d", user2 = "a";
		ClientSocket clientSocket1 = new ClientSocket();
		ClientSocket clientSocket2 = new ClientSocket();

		ChangeGroupReq.Builder builder = ChangeGroupReq.newBuilder();
		builder.setChangeType(ChangeType.DELETE);
		builder.setGroupId(groupId);
		// builder.addUserId("d");
		// builder.addUserId("e");

		// 登陆
		assertEquals(clientSocket1.login(user1, user1), LoginRsp.ResultCode.SUCCESS);
		assertEquals(clientSocket2.login(user2, user2), LoginRsp.ResultCode.SUCCESS);
		System.out.println(user1 + " , " + user2 + " login");

		// 请求自删
		ChangeGroupRsp response = sendRequest(clientSocket1, builder);
		assertEquals(response.getResultCode(), ChangeGroupRsp.ResultCode.SUCCESS);

		// user2上线收消息
		byte[] byteArray = clientSocket2.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_SYNC);
		assertNotNull(byteArray);
		ChangeGroupSync sync = ChangeGroupSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		System.out.println("CHANGE_GROUP_SYNC : " + user2 + " : " + ClientSocket.getGroupItemInfo(sync.getGroupItem()));

		// 获取user1的系统退出消息
		byteArray = clientSocket2.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC);
		assertNotNull(byteArray);
		ChatItem chatItem = ReceiveChatSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray)).getChatData(0);
		System.out.println("CHANGE_GROUP_SYNC : " + user2 + " : " + ClientSocket.getChatItemInfo(chatItem));

		clientSocket1.close();
		clientSocket2.close();
	}

	/**
	 * 无权限修改群数据
	 * 
	 * @author Feng
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	// @Test
	public void reviseGroupInfo1() throws UnknownHostException, IOException {
		String groupId = "13";
		ClientSocket clientSocket1 = new ClientSocket();

		ChangeGroupReq.Builder builder = ChangeGroupReq.newBuilder();
		builder.setChangeType(ChangeType.UPDATE_INFO);
		builder.setGroupId(groupId);
		builder.setGroupName("Fucking");

		// 无权限添加
		ChangeGroupRsp response = sendRequest(clientSocket1, builder);
		assertEquals(response.getResultCode(), ChangeGroupRsp.ResultCode.NO_AUTHORITY);

		clientSocket1.close();

	}

	/**
	 * 有权限修改群数据
	 * 
	 * @author Feng
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	 @Test
	public void reviseGroupInfo2() throws UnknownHostException, IOException {
		String groupId = "13";
		String user1 = "a", user2 = "b";
		ClientSocket clientSocket1 = new ClientSocket();
		ClientSocket clientSocket2 = new ClientSocket();

		ChangeGroupReq.Builder builder = ChangeGroupReq.newBuilder();
		builder.setChangeType(ChangeType.UPDATE_INFO);
		builder.setGroupId(groupId);
		builder.setGroupName("Fucking");

		// 登陆
//		assertEquals(clientSocket2.login(user1, user1), LoginRsp.ResultCode.SUCCESS);
		assertEquals(clientSocket1.login(user1, user1), LoginRsp.ResultCode.SUCCESS);
		assertEquals(clientSocket2.login(user2, user2), LoginRsp.ResultCode.SUCCESS);
		System.out.println(user1 + " , " + user2 + " login");

		// 请求修改
		ChangeGroupRsp response = sendRequest(clientSocket1, builder);
		assertEquals(response.getResultCode(), ChangeGroupRsp.ResultCode.SUCCESS);
		System.out.println("请求完毕!");

		// user1上线收同步数据包
		byte[] byteArray = clientSocket1.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_SYNC);
		assertNotNull(byteArray);
		ChangeGroupSync sync = ChangeGroupSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		System.out.println("CHANGE_GROUP_SYNC : " + user1 + " : " + ClientSocket.getGroupItemInfo(sync.getGroupItem()));

		// user2上线收同步数据包
		byteArray = clientSocket2.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_SYNC);
		assertNotNull(byteArray);
		sync = ChangeGroupSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		System.out.println("CHANGE_GROUP_SYNC : " + user2 + " : " + ClientSocket.getGroupItemInfo(sync.getGroupItem()));

		// 获取系统消息
		byteArray = clientSocket2.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC);
		assertNotNull(byteArray);
		ChatItem chatItem = ReceiveChatSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray)).getChatData(0);
		System.out.println("CHANGE_GROUP_SYNC : " + user2 + " : " + ClientSocket.getChatItemInfo(chatItem));

		clientSocket1.close();
		clientSocket2.close();

	}
}
