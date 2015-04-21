package JUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.PersonalSettingsMsg;
import protocol.Msg.LoginMsg.LoginRsp;
import protocol.Msg.PersonalSettingsMsg.PersonalSettingsRsp;
import server.NetworkPacket;


/**
 * 对个人设置功能的测试
 * 
 * @author wangfei
 * 
 */
public class TestPersonalSettings {
	/**
	 * 测试个人设置
	 * 
	 * @author wangfei
	 * @throws IOException
	 */
	@Test
	public void testPersonalSettings() throws IOException {
		String user1="1",password1="1";
		System.out.println("TestPersonalSettings1:只设置昵称");
		PersonalSettingsRsp personalSettingsRsp1 = getResponse(user1,password1,"newname-test1",null,0);
		System.out.println("服务器返回结果:"+personalSettingsRsp1.getResultCode().toString());
		assertEquals(personalSettingsRsp1.getResultCode().getNumber(),PersonalSettingsRsp.ResultCode.SUCCESS_VALUE);
		
		String user2="2",password2="1";
		System.out.println("TestPersonalSettings2:只设置密码");
		PersonalSettingsRsp personalSettingsRsp2 = getResponse(user2,password2,null,"newpassword-test2",0);
		System.out.println("服务器返回结果:"+personalSettingsRsp2.getResultCode().toString());
		assertEquals(personalSettingsRsp2.getResultCode().getNumber(),PersonalSettingsRsp.ResultCode.SUCCESS_VALUE);
		
		String user3="3",password3="1";
		System.out.println("TestPersonalSettings3:只设置头像");
		PersonalSettingsRsp personalSettingsRsp3 = getResponse(user3,password3,null,null,1);
		System.out.println("服务器返回结果:"+personalSettingsRsp3.getResultCode().toString());
		assertEquals(personalSettingsRsp3.getResultCode().getNumber(),PersonalSettingsRsp.ResultCode.SUCCESS_VALUE);
	}
	
	private PersonalSettingsRsp getResponse(String user,String password,String newUserName,String newPassword,int newHeadInx) throws IOException{
		ClientSocket clientSocket = new ClientSocket();
		byte[] response;
		// 登陆
		if (clientSocket.login(user, password) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");
		
		PersonalSettingsMsg.PersonalSettingsReq.Builder builder = PersonalSettingsMsg.PersonalSettingsReq.newBuilder();
		if(newUserName != null)
			builder.setUserName(newUserName);
		if(newPassword != null)
			builder.setUserPassword(newPassword);
		if(newHeadInx != 0)
			builder.setHeadIndex(newHeadInx);
		
		byte[] byteArray = NetworkPacket.packMessage(ProtoHead.ENetworkMessage.PERSONALSETTINGS_REQ.getNumber(), builder.build()
						.toByteArray());
		//发消息
		clientSocket.writeToServer(byteArray);
		
		//接收回复
		while (true) {
			response = clientSocket.readFromServerWithoutKeepAlive();
			ProtoHead.ENetworkMessage type = NetworkPacket.getMessageType(response);
			if(ProtoHead.ENetworkMessage.PERSONALSETTINGS_RSP != type)
				continue;
			
			PersonalSettingsRsp personalSettingsRsp = PersonalSettingsRsp.parseFrom(NetworkPacket.getMessageObjectBytes(response));
			clientSocket.close();
			return personalSettingsRsp;
		}	
	}
}
