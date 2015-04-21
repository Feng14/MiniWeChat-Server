package JUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.UnknownHostException;
import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.AddFriendMsg;
import protocol.Msg.AddFriendMsg.AddFriendRsp;
import protocol.Msg.LoginMsg.LoginRsp;
import server.NetworkPacket;

/**
 * 对添加好友的测试
 * @author wangfei
 *
 */
public class TestAddFriend {	
	/**
	 * 测试添加好友
	 * @author wangfei
	 * @throws IOException
	 */
	@Test
	public void testAddFriend() throws IOException{
		System.out.println("TestAddFriend1:双方不是好友关系添加好友");
		String user1="a3",password1="aa",friend1="a";
		AddFriendRsp addFriendRsp1 = getResponse(user1,password1,friend1);
		System.out.println("服务器返回结果:"+addFriendRsp1.getResultCode().toString());
		assertEquals(addFriendRsp1.getResultCode().getNumber(), AddFriendRsp.ResultCode.SUCCESS_VALUE);
		
		System.out.println("TestAddFriend2:双方已经是好友关系继续添加好友");
		String user2="a",password2="a",friend2="newuser1";
		AddFriendRsp addFriendRsp2 = getResponse(user2,password2,friend2);
		System.out.println("服务器返回结果:"+addFriendRsp2.getResultCode().toString());
		assertEquals(addFriendRsp2.getResultCode().getNumber(), AddFriendRsp.ResultCode.SUCCESS_VALUE);
		
		System.out.println("TestAddFriend3:添加不存在的用户为好友");
		String user3="newuser1",password3="123",friend3="ttttttt";
		AddFriendRsp addFriendRsp3 = getResponse(user3,password3,friend3);
		System.out.println("服务器返回结果:"+addFriendRsp3.getResultCode().toString());
		assertEquals(addFriendRsp3.getResultCode().getNumber(), AddFriendRsp.ResultCode.FAIL_VALUE);
		
	}
	
	private AddFriendRsp getResponse(String user,String password,String friend) throws UnknownHostException, IOException{
		ClientSocket clientSocket = new ClientSocket();
		byte[] response;
		// 登陆
		if (clientSocket.login(user, password) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		AddFriendMsg.AddFriendReq.Builder builder = AddFriendMsg.AddFriendReq.newBuilder();
		builder.setFriendUserId(friend);

		byte[] byteArray = NetworkPacket.packMessage(ProtoHead.ENetworkMessage.ADD_FRIEND_REQ.getNumber(), builder.build()
						.toByteArray());
		//发消息
		clientSocket.writeToServer(byteArray);
		
		//接收回复
		while (true) {
			response = clientSocket.readFromServerWithoutKeepAlive();
			ProtoHead.ENetworkMessage type = NetworkPacket.getMessageType(response);
			if(ProtoHead.ENetworkMessage.ADD_FRIEND_RSP != type)
				continue;
			
			AddFriendRsp addFriendRsp = AddFriendRsp.parseFrom(NetworkPacket.getMessageObjectBytes(response));
			clientSocket.close();
			return addFriendRsp;
		}
	}
}
