package JUnit;

import static org.junit.Assert.*;
import java.io.IOException;
import java.net.UnknownHostException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import protocol.Msg.LoginMsg;
import protocol.Msg.LogoutMsg;
import protocol.Msg.LoginMsg.LoginRsp;
import protocol.Msg.LogoutMsg.LogoutRsp;
import client.SocketClientTest;
import server.NetworkPacket;

/**
 * 对登陆功能的测试（要先开服务器）
 * @author Feng
 *
 */
public class TestLogin {
	private String user = "a";

	@Before
	public void init() throws UnknownHostException, IOException {
//		client.link();
	}

	/**
	 * 测试登陆功能
	 * 测试账号错误，密码错误
	 * @author Feng
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	@Test
	@Ignore
	public void testLogin() throws UnknownHostException, IOException {
		ClientSocket client = new ClientSocket();
		String userId = "a";
		
		System.out.println("Start Test1 Login!");
		LoginRsp.ResultCode resultCode = client.login(userId, userId);
		assertEquals(resultCode, LoginMsg.LoginRsp.ResultCode.SUCCESS);

//		resultCode = client.login(userId + "error", userId);
//		assertEquals(resultCode, LoginMsg.LoginRsp.ResultCode.FAIL);
//
//		resultCode = client.login(userId, userId + "error");
//		assertEquals(resultCode, LoginMsg.LoginRsp.ResultCode.FAIL);
//		client.close();
	}
	
	/**
	 * 测试登陆，下线，登陆
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	@Test
	@Ignore
	public void testLogin2() throws UnknownHostException, IOException {
		ClientSocket client = new ClientSocket();
		
		String userId1 = "c", userId2 = "d";
		System.out.println("Start Test2 Login!");
		LoginRsp.ResultCode resultCode = client.login(userId1, userId1);
		assertEquals(LoginMsg.LoginRsp.ResultCode.SUCCESS, resultCode);
		
		// 下线
		LogoutRsp.ResultCode logoutResultCode = client.logout();
		assertEquals(LogoutMsg.LogoutRsp.ResultCode.SUCCESS, logoutResultCode);
		
		// 再登录
		resultCode = client.login(userId2, userId2);
		assertEquals(LoginMsg.LoginRsp.ResultCode.SUCCESS, resultCode);
		
		// 下线
		logoutResultCode = client.logout();
		assertEquals(LogoutRsp.ResultCode.SUCCESS, logoutResultCode);
		
		// 再登录
		resultCode = client.login(userId1, userId1);
		assertEquals(LoginMsg.LoginRsp.ResultCode.SUCCESS, resultCode);
		
		client.close();
	}
}
