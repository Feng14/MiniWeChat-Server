package JUnit;

import static org.junit.Assert.*;
import java.io.IOException;
import java.net.UnknownHostException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import protocol.Msg.LoginMsg;
import protocol.Msg.LogoutMsg;
import client.SocketClientTest;
import server.NetworkMessage;

/**
 * 对登陆功能的测试（要先开服务器）
 * @author Feng
 *
 */
public class TestLogin {
	public SocketClientTest client;

	@Before
	public void init() throws UnknownHostException, IOException {
		client = new SocketClientTest();
		client.link();
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
		System.out.println("Start Test Login!");
		byte[] resultBytes = client.testLogin_JUint("a", "a");
		LoginMsg.LoginRsp responseObject = LoginMsg.LoginRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), LoginMsg.LoginRsp.ResultCode.SUCCESS.toString());

		resultBytes = client.testLogin_JUint("aa", "aa");
		responseObject = LoginMsg.LoginRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), LoginMsg.LoginRsp.ResultCode.FAIL.toString());

		resultBytes = client.testLogin_JUint("a", "aaa");
		responseObject = LoginMsg.LoginRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), LoginMsg.LoginRsp.ResultCode.FAIL.toString());
	}
	
	/**
	 * 测试登陆，下线，登陆
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	@Test
	public void testLogin2() throws UnknownHostException, IOException {
		System.out.println("Start Test Login!");
		byte[] resultBytes = client.testLogin_JUint("a", "a");
		LoginMsg.LoginRsp responseObject = LoginMsg.LoginRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), LoginMsg.LoginRsp.ResultCode.SUCCESS.toString());
		
		// 下线
		resultBytes = client.testLogout_JUnit();
		LogoutMsg.LogoutRsp responseObject2 = LogoutMsg.LogoutRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), LogoutMsg.LogoutRsp.ResultCode.SUCCESS.toString());
		
		// 再登录
		resultBytes = client.testLogin_JUint("b", "b");
		responseObject = LoginMsg.LoginRsp.parseFrom(NetworkMessage.getMessageObjectBytes(resultBytes));
		assertEquals(responseObject.getResultCode().toString(), LoginMsg.LoginRsp.ResultCode.SUCCESS.toString());
	}
}
