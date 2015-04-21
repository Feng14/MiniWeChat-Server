package JUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.GetPersonalInfoMsg;
import protocol.Msg.GetPersonalInfoMsg.GetPersonalInfoRsp;
import protocol.Msg.LoginMsg.LoginRsp;
import server.NetworkPacket;



public class TestGetPersonalInfo {
	/**
	 * 测获取个人信息
	 * @throws IOException
	 * @author wangfei 
	 * @time 2015-03-26
	 */
	@Test
	public void testGetPersonalInfo() throws IOException{
		String user1="3",password1="1";
		System.out.println("TestGetPersonalInfo1:只获取个人信息");
		GetPersonalInfoRsp getPersonalInfoRsp1 = getResponse(user1,password1,true,false,false);
		System.out.println("服务器返回结果:"+getPersonalInfoRsp1.getResultCode().toString());
		assertEquals(getPersonalInfoRsp1.getResultCode().getNumber(),GetPersonalInfoRsp.ResultCode.SUCCESS_VALUE);
		
		String user2="1",password2="1";
		System.out.println("TestGetPersonalInfo2:获取个人信息、好友信息");
		GetPersonalInfoRsp getPersonalInfoRsp2 = getResponse(user2,password2,true,true,false);
		System.out.println("服务器返回结果:"+getPersonalInfoRsp2.getResultCode().toString());
		assertEquals(getPersonalInfoRsp2.getResultCode().getNumber(),GetPersonalInfoRsp.ResultCode.SUCCESS_VALUE);
		
		String user3="a3",password3="aa";
		System.out.println("TestGetPersonalInfo3:获取个人信息、好友信息、群聊信息");
		GetPersonalInfoRsp getPersonalInfoRsp3 = getResponse(user3,password3,true,true,true);
		System.out.println("服务器返回结果:"+getPersonalInfoRsp3.getResultCode().toString());
		assertEquals(getPersonalInfoRsp3.getResultCode().getNumber(),GetPersonalInfoRsp.ResultCode.SUCCESS_VALUE);

	}
	
	private GetPersonalInfoRsp getResponse(String user,String password,boolean userInfo,boolean friendInfo,boolean groupInfo) throws UnknownHostException, IOException{
		ClientSocket clientSocket = new ClientSocket();
		byte[] response;
		// 登陆
		if (clientSocket.login(user, password) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");
		GetPersonalInfoMsg.GetPersonalInfoReq.Builder builder = GetPersonalInfoMsg.GetPersonalInfoReq.newBuilder();
		builder.setUserInfo(userInfo);
		builder.setFriendInfo(friendInfo);
		builder.setGroupInfo(groupInfo);
		
		byte[] byteArray = NetworkPacket.packMessage(ProtoHead.ENetworkMessage.GET_PERSONALINFO_REQ.getNumber(), builder.build()
						.toByteArray());
		//发消息
		clientSocket.writeToServer(byteArray);
		
		//接收回复
		while (true) {
			response = clientSocket.readFromServerWithoutKeepAlive();
			ProtoHead.ENetworkMessage type = NetworkPacket.getMessageType(response);
			if(ProtoHead.ENetworkMessage.GET_PERSONALINFO_RSP != type)
				continue;
			
			GetPersonalInfoRsp getPersonalInfoFriendRsp = GetPersonalInfoRsp.parseFrom(NetworkPacket.getMessageObjectBytes(response));
			clientSocket.close();
			return getPersonalInfoFriendRsp;
		}
		
	}

}
