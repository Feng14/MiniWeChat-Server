package JUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.DeleteFriendMsg;
import protocol.Msg.DeleteFriendMsg.DeleteFriendRsp;
import protocol.Msg.LoginMsg.LoginRsp;
import server.NetworkPacket;

/**
 * 对删除好友的测试
 * @author wangfei
 *
 */
public class TestDeleteFriend {
	/**
	 * 测试删除好友
	 * @author wangfei
	 * @throws IOException
	 */
	@Test
	public void testDeleteFriend() throws IOException{
		System.out.println("TestDeleteFriend1:双方已经是好友关系删除好友");
		String user1="a3",password1="aa",friend1="a";
		DeleteFriendRsp deleteFriendRsp1 = getResponse(user1,password1,friend1);
		System.out.println("服务器返回结果:"+deleteFriendRsp1.getResultCode().toString());
		assertEquals(deleteFriendRsp1.getResultCode().getNumber(), DeleteFriendRsp.ResultCode.SUCCESS_VALUE);
		
		System.out.println("TestDeleteFriend2:双方不是好友关系删除好友");
		String user2="a3",password2="aa",friend2="2";
		DeleteFriendRsp deleteFriendRsp2 = getResponse(user2,password2,friend2);
		System.out.println("服务器返回结果:"+deleteFriendRsp2.getResultCode().toString());
		assertEquals(deleteFriendRsp2.getResultCode().getNumber(), DeleteFriendRsp.ResultCode.SUCCESS_VALUE);
	}
	
	private DeleteFriendRsp getResponse(String user,String password,String friend) throws UnknownHostException, IOException{
		ClientSocket clientSocket = new ClientSocket();
		byte[] response;
		// 登陆
		if (clientSocket.login(user, password) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		DeleteFriendMsg.DeleteFriendReq.Builder builder = DeleteFriendMsg.DeleteFriendReq.newBuilder();
		builder.setFriendUserId(friend);
		
		byte[] byteArray = NetworkPacket.packMessage(ProtoHead.ENetworkMessage.DELETE_FRIEND_REQ.getNumber(), builder.build()
						.toByteArray());
		//发消息
		clientSocket.writeToServer(byteArray);
		
		//接收回复
		while (true) {
			response = clientSocket.readFromServerWithoutKeepAlive();
			ProtoHead.ENetworkMessage type = NetworkPacket.getMessageType(response);
			if(ProtoHead.ENetworkMessage.DELETE_FRIEND_RSP != type)
				continue;
			
			DeleteFriendRsp deleteFriendRsp = DeleteFriendRsp.parseFrom(NetworkPacket.getMessageObjectBytes(response));
			clientSocket.close();
			return deleteFriendRsp;
		}
	}

}
