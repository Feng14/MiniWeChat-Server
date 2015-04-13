package JUnit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.sun.org.apache.bcel.internal.generic.RETURN;

import protocol.ProtoHead;
import protocol.Msg.LogoutMsg;
import protocol.Msg.LoginMsg.LoginReq;
import protocol.Msg.LoginMsg.LoginRsp;
import protocol.Msg.LogoutMsg.LogoutRsp;

import server.NetworkPacket;
import tools.DataTypeTranslater;

/**
 * 作为测试用例调用Socket前的初始化
 * 
 * @author Feng
 * 
 */
public class ClientSocket {
	public static final int HEAD_INT_SIZE = 4;
	public Socket socket;
	public InputStream inputStream;
	public OutputStream outputStream;


//	 String host = "192.168.45.17"; // 要连接的服务端IP地址
	//public static final String host = "104.224.165.21"; // 要连接的服务端IP地址

	String host = "127.0.0.1"; // 要连接的服务端IP地址
//	public static final String host = "104.224.165.21"; // 要连接的服务端IP地址

	// String host = "192.168.1.103"; // 要连接的服务端IP地址
	// String host = "192.168.45.11"; // 要连接的服务端IP地址
//	String host = "192.168.45.34"; // 要连接的服务端IP地址

	int port = 8081; // 要连接的服务端对应的监听端口

	public ClientSocket() throws UnknownHostException, IOException {
		link();
	}

	/**
	 * 连接Socket
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @author Feng
	 */
	public void link() throws UnknownHostException, IOException {
		socket = new Socket(host, port);
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
	}
	
	/**
	 * 关闭连接
	 * @throws IOException 
	 */
	public void close() throws IOException{
		inputStream.close();
		outputStream.close();
		socket.close();
	}

	/**
	 * 处理服务器回复问题
	 * 
	 * @author Feng
	 * @return byte[]
	 * @throws IOException
	 */
	public byte[] readFromServer() throws IOException {
		byte[] byteArray = new byte[500];

		inputStream.read(byteArray);
		byteArray = cutResult(byteArray);
		return byteArray;
		
		
//		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//		int data;
//		while (true) {
//			if ((data = inputStream.read()) == -1)
//				break;
//			byteArrayOutputStream.write(data);
//		}
		
//		inputStream.read(byteArray);
//		byteArray = cutResult(byteArray);

//		return byteArray;
//		byte[] byteArray = byteArrayOutputStream.toByteArray();
//		byteArrayOutputStream.close();
//		System.out.println("length : " + byteArray.length);
//		System.out.println(byteArray);
//		
//		return byteArray;
	}

	/**
	 * 读取服务器发来的非心跳包
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] readFromServerWithoutKeepAlive() throws IOException {
		byte[] byteArray;

		while (true) {
			byteArray = readFromServer();
			// 不是KeepAlive（心跳包就返回）
			if (NetworkPacket.getMessageType(byteArray) != ProtoHead.ENetworkMessage.KEEP_ALIVE_SYNC)
				return byteArray;

		}
	}

	/**
	 * 向服务器传输byte[]
	 * 
	 * @param arrayBytes
	 * @throws IOException
	 */
	public void writeToServer(byte[] arrayBytes) throws IOException {
		// outputStream = socket.getOutputStream();
		outputStream.write(arrayBytes);
		// outputStream.close();
	}

	/**
	 * 用于剪切从服务器发过来的byte[]
	 * 
	 * @param byteArray
	 * @return
	 */
	public byte[] cutResult(byte[] byteArray) {
		int size = DataTypeTranslater.bytesToInt(byteArray, 0);
		byte[] result = new byte[size];
		for (int i = 0; i < size; i++)
			result[i] = byteArray[i];

		return result;
	}

	/**
	 * 登陆
	 * 
	 * @param userId
	 * @param userPassword
	 * @return
	 * @throws IOException
	 * @author Feng
	 */
	public LoginRsp.ResultCode login(String userId, String userPassword) throws IOException {
		byte[] response;

		LoginReq.Builder loginBuilder = LoginReq.newBuilder();
		loginBuilder.setUserId(userId);
		loginBuilder.setUserPassword(userPassword);

		writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.LOGIN_REQ_VALUE, loginBuilder.build().toByteArray()));
		for (int i=0; i<10; i++) {
			response = readFromServerWithoutKeepAlive();
			if (NetworkPacket.getMessageType(response) != ProtoHead.ENetworkMessage.LOGIN_RSP)
				continue;

			LoginRsp loginResponse = LoginRsp.parseFrom(NetworkPacket.getMessageObjectBytes(response));
			return loginResponse.getResultCode();
		}
		return LoginRsp.ResultCode.FAIL;
	}
	
	
	public LogoutRsp.ResultCode logout() throws IOException{
		LogoutMsg.LogoutReq.Builder builder = LogoutMsg.LogoutReq.newBuilder();
		byte[] byteArray = NetworkPacket.packMessage(ProtoHead.ENetworkMessage.LOGOUT_REQ.getNumber(), builder.build()
				.toByteArray());

		writeToServer(byteArray);
		
		for (int i=0; i<10; i++) {
			byteArray = readFromServerWithoutKeepAlive();
			if (NetworkPacket.getMessageType(byteArray) == ProtoHead.ENetworkMessage.LOGOUT_RSP) {
				return LogoutRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray)).getResultCode();
			}
		}
		return LogoutRsp.ResultCode.FAIL;
	}
	
	/**
	 * 测试用，查看收到的所有byte
	 * @param arrayBytes
	 * @author Feng
	 */
	private void showBytes(byte[] arrayBytes) {
		for (byte b : arrayBytes)
			System.out.print(b + "  ");
		System.out.println();
	}
}
