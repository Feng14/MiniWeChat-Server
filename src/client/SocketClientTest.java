package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

import protocol.KeepAliveMsg;
import protocol.LoginMsg;
import protocol.PersonalSettingsMsg;
import protocol.ProtoHead;
import protocol.RegisterMsg;
import protocol.RegisterMsg.RegisterReq;
import server.NetworkMessage;
import tools.DataTypeTranslater;

public class SocketClientTest {

	public static final int HEAD_INT_SIZE = 4;
	public Socket socket;
	public InputStream inputStream;
	public OutputStream outputStream;

	String host = "192.168.45.34"; // 要连接的服务端IP地址
	// String host = "192.168.45.37"; // 要连接的服务端IP地址
	int port = 8080; // 要连接的服务端对应的监听端口

	public static void main(String args[]) throws IOException {
		new SocketClientTest();
	}

	public SocketClientTest() {
		// 为了简单起见，所有的异常都直接往外抛
		String host = "192.168.45.55"; // 要连接的服务端IP地址
		// String host = "192.168.45.37"; // 要连接的服务端IP地址
		int port = 8080; // 要连接的服务端对应的监听端口
		// 与服务端建立连接
		try {
			// socket = new Socket(host, port);
			// 建立连接后就可以往服务端写数据了
			// Writer writer = new OutputStreamWriter(socket.getOutputStream());

			// 测
			KeepAliveMsg.KeepAliveSyncPacket.Builder keepAliveSyncBuilder = KeepAliveMsg.KeepAliveSyncPacket.newBuilder();
			keepAliveSyncBuilder.setA(1);
			keepAliveSyncBuilder.setB(true);
			keepAliveSyncBuilder.setC("wangxuanyi");
			byte[] responseByteArray = keepAliveSyncBuilder.build().toByteArray();

			byte[] messageBytes = new byte[HEAD_INT_SIZE * 2 + responseByteArray.length];
			System.out.println("length:" + messageBytes.length);

			int offset = 0;
			// 1. 插入Size
			byte[] sizeBytes = DataTypeTranslater.intToByte(messageBytes.length);
			for (int i = 0; i < HEAD_INT_SIZE; i++)
				messageBytes[i + offset] = sizeBytes[i];
			offset += HEAD_INT_SIZE;

			// 2.插入表头Type
			byte[] typeBytes = DataTypeTranslater.intToByte(ProtoHead.ENetworkMessage.KEEP_ALIVE_SYNC.getNumber());
			for (int i = 0; i < HEAD_INT_SIZE; i++)
				messageBytes[i + offset] = typeBytes[i];
			offset += HEAD_INT_SIZE;

			// 3.插入Protobuf内容
			for (int i = 0; i < responseByteArray.length; i++)
				messageBytes[i + offset] = responseByteArray[i];

			System.out.println("Test:");
			// outputStream = socket.getOutputStream();

			byte[] second = new byte[messageBytes.length * 2];
			for (int i = 0; i < messageBytes.length * 2; i++)
				second[i] = messageBytes[i % messageBytes.length];

			// outputStream.write(second);
			// System.out.println("发送完毕");

			// 接收
			System.out.println("开始接收");
			// while (true) {
			// System.out.println("收到: " + readFromServer(socket));
			// }
			// InputStream in = socket.getInputStream();
			// byte[] byteArray = new byte[200];

			// for (int readTimes = 0; readTimes < 5; readTimes++) {
			// System.out.println(in.read(byteArray));
			// for (int i = 0; i < 4; i++)
			// System.out.println(in.read());
			// System.out.println(byteArray);
			// for (int i=0; i<byteArray.length; i++)
			// System.err.println(byteArray[i]);

			// int size = DataTypeTranslater.bytesToInt(byteArray, 0);
			// int reqOffset = 0;
			// do {
			//
			// ProtoHead.ENetworkMessage messageType =
			// ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(
			// byteArray, 4));
			// System.out.println("client接收到的数据长度：" + size + " 字节");
			// System.out.println("client接收到的数据类型：" + messageType.toString());
			//
			// offset = HEAD_INT_SIZE * 2;
			// int contentLength = size - offset;
			// System.out.println("内容的长度为：" + contentLength + " 字节");
			// // byte[] contentbytes = new byte[contentLength];
			// // for (int i = 0; i < contentLength; i++)
			// // contentbytes[i] = byteArray[offset + i];
			//
			// KeepAliveMsg.KeepAliveSyncPacket packet =
			// KeepAliveMsg.KeepAliveSyncPacket.parseFrom(byteArray);
			// System.out.println("client收到包的内容是：" + "   " +
			// messageType.toString() + "  " + packet.getA() + "   "
			// + packet.getB() + "   " + packet.getC());
			//
			// System.out.println("接收完毕");
			//
			// reqOffset += size;
			// size = DataTypeTranslater.bytesToInt(byteArray, reqOffset);
			// System.err.println(size);
			// } while (size > 0);
			//
			// // 关闭流
			// // br.close();
			// }

			// in.close();
			// writer.close();
			// socket.close();
			// inputStream = socket.getInputStream();
//			socket = new Socket(host, port);
//			inputStream = socket.getInputStream();
//			outputStream = socket.getOutputStream();
			// 测注册
			
			//testRegister();
			
			// 测登陆
			//testLogin();
			//测试个人设置
			testPersonalSettings();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		new Thread(new readThread()).start();
	}

	// 处理服务器回复问题

	public byte[] readFromServer(Socket socket) throws IOException {

//		inputStream = socket.getInputStream();
		byte[] byteArray = new byte[200];
		// System.out.println(in.read(byteArray));
		inputStream.read(byteArray);
		// System.out.println("client 收到Server 发来的 ： " + byteArray);
		return byteArray;
	}
	
	public void writeToServer(byte[] arrayBytes) throws IOException {
//		outputStream = socket.getOutputStream();
		outputStream.write(arrayBytes);
//		outputStream.close();
	}

	/**
	 * 永久读线程
	 * 
	 * @author Feng
	 * 
	 */
	class readThread implements Runnable {
		@Override
		public void run() {
			try {
//				socket = new Socket(host, port);
				while (true) {
					Thread.sleep(1000);
					byte[] arrayBytes = readFromServer(socket);
					System.out.println("client 收到Server 发来的 ： " + arrayBytes);

					System.out.println("size:" + DataTypeTranslater.bytesToInt(arrayBytes, 0));
					System.out.println("Type:"
							+ ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(arrayBytes, HEAD_INT_SIZE))
									.toString());
					
					//发回去
					writeToServer(arrayBytes);
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 测试注册功能
	 * @author Feng
	 */
	public void testRegister(){
		RegisterMsg.RegisterReq.Builder builder = RegisterMsg.RegisterReq.newBuilder();
		builder.setUserId("a");
		builder.setUserPassword("aa");
		builder.setUserName("aaa");
		System.out.println("Start Test Register!");
		try {
			socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			
			byte[] byteArray = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.REGISTER_REQ.getNumber(), builder.build().toByteArray());
//			outputStream = socket.getOutputStream();
			writeToServer(byteArray);
			
//			inputStream = socket.getInputStream();
			while (true) {
				byteArray = readFromServer(socket);
				int size = DataTypeTranslater.bytesToInt(byteArray, 0);
				System.out.println("size: " + size);
				
				ProtoHead.ENetworkMessage type = ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(byteArray, HEAD_INT_SIZE));
				System.out.println("Type : " + type.toString());
				
				if (type == ProtoHead.ENetworkMessage.REGISTER_RSP) {
					byte[] objBytes = new byte[size - NetworkMessage.getMessageObjectStartIndex()];
					for (int i=0; i<objBytes.length; i++)
						objBytes[i] = byteArray[NetworkMessage.getMessageObjectStartIndex() + i];
					
					RegisterMsg.RegisterRsp response = RegisterMsg.RegisterRsp.parseFrom(objBytes);
					
					System.out.println("Response : " + RegisterMsg.RegisterRsp.ResultCode.valueOf(response.getResultCode().getNumber()));
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 测试登陆功能
	 */
	public void testLogin(){

		LoginMsg.LoginReq.Builder builder = LoginMsg.LoginReq.newBuilder();
		builder.setUserId("aa");
		builder.setUserPassword("aa");
		System.out.println("Start Test Login!");
		try {
			socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			
			byte[] byteArray = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.LOGIN_REQ.getNumber(), builder.build().toByteArray());
//			outputStream = socket.getOutputStream();
			writeToServer(byteArray);
			
//			inputStream = socket.getInputStream();
			while (true) {
				byteArray = readFromServer(socket);
				int size = DataTypeTranslater.bytesToInt(byteArray, 0);
				System.out.println("size: " + size);
				
				ProtoHead.ENetworkMessage type = ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(byteArray, HEAD_INT_SIZE));
				System.out.println("Type : " + type.toString());
				
				if (type == ProtoHead.ENetworkMessage.LOGIN_RSP) {
					byte[] objBytes = new byte[size - NetworkMessage.getMessageObjectStartIndex()];
					for (int i=0; i<objBytes.length; i++)
						objBytes[i] = byteArray[NetworkMessage.getMessageObjectStartIndex() + i];
					
					LoginMsg.LoginRsp response = LoginMsg.LoginRsp.parseFrom(objBytes);
					
					System.out.println("Response : " + LoginMsg.LoginRsp.ResultCode.valueOf(response.getResultCode().getNumber()));
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void  testPersonalSettings(){
		PersonalSettingsMsg.PersonalSettingsReq.Builder builder = PersonalSettingsMsg.PersonalSettingsReq.newBuilder();
		builder.setUserId("Fuck");
		//builder.setUserName("ssss");
		//builder.setUserPassword("s123");
		builder.setHeadIndex(1);
		System.out.println("start personalSettings test!");
		try{
			Socket socket = new Socket(host,port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			
			byte[] byteArray = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.PERSONALSETTINGS_REQ.getNumber(), builder.build().toByteArray());
			writeToServer(byteArray);
			while (true) {
				byteArray = readFromServer(socket);
				int size = DataTypeTranslater.bytesToInt(byteArray, 0);
				System.out.println("size: " + size);
				
				ProtoHead.ENetworkMessage type = ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(byteArray, HEAD_INT_SIZE));
				System.out.println("Type : " + type.toString());
				
				if (type == ProtoHead.ENetworkMessage.PERSONALSETTINGS_RSP) {
					byte[] objBytes = new byte[size - NetworkMessage.getMessageObjectStartIndex()];
					for (int i=0; i<objBytes.length; i++)
						objBytes[i] = byteArray[NetworkMessage.getMessageObjectStartIndex() + i];
					
					PersonalSettingsMsg.PersonalSettingsRsp response = PersonalSettingsMsg.PersonalSettingsRsp.parseFrom(objBytes);
					
					System.out.println("Response : " + PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.valueOf(response.getResultCode().getNumber()));
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
