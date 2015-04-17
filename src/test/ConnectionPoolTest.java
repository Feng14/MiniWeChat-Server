package test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import protocol.ProtoHead;
import protocol.Msg.LoginMsg;
import protocol.Msg.LoginMsg.LoginRsp;
import server.NetworkPacket;
import tools.DataTypeTranslater;


public class ConnectionPoolTest {
	Socket socket;
	InputStream inputStream;
	OutputStream outputStream;
	private static String host = "127.0.0.1";
	private static int port = 8081;
	
	public static void main(String args[]){
		//测试多用户同时在线的情况时  连接池是否正常
		ConnectionPoolTest t = new ConnectionPoolTest();
		t.login("11", "1");
//		t.login("2", "1");
//		t.login("3", "1");
//		t.login("4", "1");
//		t.login("5", "1");
//		t.login("6", "1");
//		t.login("7", "1");
//		t.login("8", "1");
//		t.login("9", "1");
//		t.login("10", "1");
//		t.login("11", "1");
	}
	private  void login(String user,String password){
		
		
		LoginMsg.LoginReq.Builder builder = LoginMsg.LoginReq.newBuilder();
		builder.setUserId(user);
		builder.setUserPassword(password);
		try {
			socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			byte[] byteArray = NetworkPacket.packMessage(ProtoHead.ENetworkMessage.LOGIN_REQ.getNumber(), builder.build().toByteArray());
			writeToServer(byteArray);
			while (true) {
				byteArray = readFromServer();
				ProtoHead.ENetworkMessage type = NetworkPacket.getMessageType(byteArray);
				if(ProtoHead.ENetworkMessage.LOGIN_RSP == type){
					LoginRsp loginRsp = LoginRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
					System.out.println(user+":登录结果:"+loginRsp.getResultCode().toString());
				}
				else{
					System.out.println(type);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public void writeToServer(byte[] arrayBytes) throws IOException {
		// outputStream = socket.getOutputStream();
		outputStream.write(arrayBytes);
		// outputStream.close();
	}

	public byte[] readFromServer() throws IOException {
		byte[] byteArray = new byte[200];
		inputStream.read(byteArray);
		byteArray = cutResult(byteArray);
		return byteArray;
	}
	
	public byte[] cutResult(byte[] byteArray) {
		int size = DataTypeTranslater.bytesToInt(byteArray, 0);
		byte[] result = new byte[size];
		for (int i = 0; i < size; i++)
			result[i] = byteArray[i];
		return result;
	}
}
