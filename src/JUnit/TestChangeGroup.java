package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;

import protocol.ProtoHead;
import protocol.Data.GroupData.GroupItem;
import protocol.Msg.ChangeGroupMsg.ChangeGroupReq;
import protocol.Msg.ChangeGroupMsg.ChangeGroupReq.ChangeType;
import protocol.Msg.ChangeGroupMsg.ChangeGroupRsp;
import protocol.Msg.ChangeGroupMsg.ChangeGroupSync;
import protocol.Msg.LoginMsg.LoginRsp;
import protocol.Msg.LogoutMsg.LogoutRsp;
import server.NetworkPacket;

/**
 * 对“修改群聊用户”功能的测试
 * 
 * @author Feng
 * 
 */
public class TestChangeGroup {
	
	private ChangeGroupRsp sendRequest(ClientSocket clientSocket, ChangeGroupReq.Builder builder) throws IOException {
		clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_REQ_VALUE, builder
				.build().toByteArray()));
		
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
//	@Test
	public void test1() throws UnknownHostException, IOException {
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
	@Test
	public void test2() throws UnknownHostException, IOException {
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
		
		// user2上线收消息
		byte[] byteArray = clientSocket2.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_SYNC);
		assertNotNull(byteArray);
		ChangeGroupSync sync = ChangeGroupSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		GroupItem groupItem = sync.getGroupItem();
		System.out.println(groupItem.toString());
		
		
		clientSocket1.close();
		clientSocket2.close();
	}

	/**
	 * 测试删除群聊成员
	 * @throws UnknownHostException
	 * @throws IOException
	 * @author Feng
	 */
//	@Test
	public void test3() throws UnknownHostException, IOException{
		String groupId = "13";
		String user1 = "e", user2 = "a";
		ClientSocket clientSocket1 = new ClientSocket();
		ClientSocket clientSocket2 = new ClientSocket();

		ChangeGroupReq.Builder builder = ChangeGroupReq.newBuilder();
		builder.setChangeType(ChangeType.DELETE);
		builder.setGroupId(groupId);
//		builder.addUserId("d");
//		builder.addUserId("e");

		clientSocket1.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_REQ_VALUE, builder
				.build().toByteArray()));
		
		// 未登录，无权限
		byte[] byteArray = clientSocket1.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_RSP);
		assertNotNull(byteArray);
		ChangeGroupRsp response = ChangeGroupRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(response.getResultCode(), ChangeGroupRsp.ResultCode.NO_AUTHORITY);
		System.out.println(response.getResultCode().toString());
		
		// 删除自己，成功
		assertEquals(clientSocket1.login(user1, user1), LoginRsp.ResultCode.SUCCESS);
		System.out.println(user1 + " login");
		assertEquals(clientSocket2.login(user2, user2), LoginRsp.ResultCode.SUCCESS);
		
		clientSocket1.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.CHANGE_GROUP_REQ_VALUE, builder
				.build().toByteArray()));
		byteArray = clientSocket1.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_RSP);
		assertNotNull(byteArray);
		response = ChangeGroupRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(response.getResultCode(), ChangeGroupRsp.ResultCode.SUCCESS);

		// user2上线收消息
		byteArray = clientSocket2.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.CHANGE_GROUP_SYNC);
		assertNotNull(byteArray);
		ChangeGroupSync sync = ChangeGroupSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		
		clientSocket1.close();
		clientSocket2.close();
	}
}
