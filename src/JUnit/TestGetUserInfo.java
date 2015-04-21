package JUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.GetUserInfoMsg;
import protocol.Msg.GetUserInfoMsg.GetUserInfoRsp;
import protocol.Msg.LoginMsg.LoginRsp;
import server.NetworkPacket;

/**
 * 对获取用户信息的测试
 * @author wangfei
 *
 */
public class TestGetUserInfo {
	/**
	 * 测获取用户信息
	 * @author wangfei
	 * @throws IOException
	 */
	@Test
	public void testGetUserInfo() throws IOException{
		String user1="a3",password1="aa",targetUserId="1";
		
		System.out.println("TestGetPersonalInfo1:获取用户信息");
		GetUserInfoRsp getUserInfoRsp1 = getResponse(user1,password1,targetUserId);
		System.out.println("服务器返回结果:"+getUserInfoRsp1.getResultCode().toString());
		assertEquals(getUserInfoRsp1.getResultCode().getNumber(),GetUserInfoRsp.ResultCode.SUCCESS_VALUE);
	}

	
	
	private GetUserInfoRsp getResponse(String user,String password,String targetUserId) throws IOException {
		ClientSocket clientSocket = new ClientSocket();
		byte[] response;
		// 登陆
		if (clientSocket.login(user, password) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");
		
		GetUserInfoMsg.GetUserInfoReq.Builder builder = GetUserInfoMsg.GetUserInfoReq.newBuilder();
		builder.addTargetUserId(targetUserId);
		
		byte[] byteArray = NetworkPacket.packMessage(ProtoHead.ENetworkMessage.GET_USERINFO_REQ.getNumber(), builder.build()
						.toByteArray());
		//发消息
		clientSocket.writeToServer(byteArray);
		
		//接收回复
		while (true) {
			response = clientSocket.readFromServerWithoutKeepAlive();
			ProtoHead.ENetworkMessage type = NetworkPacket.getMessageType(response);
			if(ProtoHead.ENetworkMessage.GET_USERINFO_RSP != type)
				continue;
			
			GetUserInfoRsp getUserInfoFriendRsp = GetUserInfoRsp.parseFrom(NetworkPacket.getMessageObjectBytes(response));
			clientSocket.close();
			return getUserInfoFriendRsp;
		}
	}
}
