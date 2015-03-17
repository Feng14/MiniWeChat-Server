package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;
import tools.DataTypeTranslater;

public class TcpServer {
	
	public static void main(String[] args) throws IOException {
		// 显示IP地址
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			System.out.println("IP地址：" + addr.getHostAddress().toString());
			System.out.println("本机名称：" + addr.getHostName().toString());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		System.out.println("端口号：8080");

		IoAcceptor acceptor = new NioSocketAcceptor();
		acceptor.setHandler(new TcpServerHandle());
		acceptor.bind(new InetSocketAddress(8080));
	}

}

class TcpServerHandle extends IoHandlerAdapter {
	public static final int HEAD_INT_SIZE = 4;
	private int count = 0;

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		cause.printStackTrace();
	}

	// 接收到新的数据
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// 接收客户端的数据
		IoBuffer ioBuffer = (IoBuffer) message;
		byte[] byteArray = new byte[ioBuffer.limit()];
		ioBuffer.get(byteArray, 0, ioBuffer.limit());
		System.out.println("messageReceived:" + new String(byteArray, "UTF-8"));

		System.out.println("byteArray.length = " + byteArray.length);
		int offset = 0;
		// 大小
		int size = DataTypeTranslater.bytesToInt(byteArray, 0);
		System.out.println("前四个byte为：");
		for (int i=0; i<4; i++)
			System.err.println(byteArray[i]);
		
		offset += HEAD_INT_SIZE;
		System.out.println("Size is :" + size);
		
		// 类型
		int typeInt = DataTypeTranslater.bytesToInt(byteArray, 4);
//		System.out.println("Type Number is " + typeInt);

		ProtoHead.ENetworkMessage messageType = ProtoHead.ENetworkMessage.valueOf(typeInt);
		System.out.println("Type is :" + messageType.toString());
		

		offset += HEAD_INT_SIZE;
//		System.out.println("now offset : " + offset);
		
		// 内容
		byte[] messageBytes = new byte[size - HEAD_INT_SIZE * 2];
		for (int i=0; i<messageBytes.length; i++)
			messageBytes[i] = byteArray[offset + i]; 

//		System.out.println("message Bytes is :" + messageBytes);

		// 反序列化
		byte[] responseByteArray = new byte[0];
		try {
			KeepAliveMsg.KeepAliveSyncPacket packet = KeepAliveMsg.KeepAliveSyncPacket.parseFrom(messageBytes);
			System.out.println("收到包：" + "   " + messageType.toString() + "  " + packet.getA() + "   " + packet.getB()
					+ "   " + packet.getC());

			// 重建--》回复
			KeepAliveMsg.KeepAliveSyncPacket.Builder keepAliveSyncBuilder = KeepAliveMsg.KeepAliveSyncPacket.newBuilder();
			int i = messageType.LoginRsp.getNumber();
			keepAliveSyncBuilder.setA(packet.getA() + 1);
			keepAliveSyncBuilder.setB(!packet.getB());
			keepAliveSyncBuilder.setC("fuck " + packet.getC());
			responseByteArray = keepAliveSyncBuilder.build().toByteArray();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		// 发送到客户端
		// byte[] responseByteArray =
		// "Hello MiniWeChat Client".getBytes("UTF-8");
		// byte[] responseByteArray = "Hello".getBytes("UTF-8");
		int returnPacketLength = HEAD_INT_SIZE * 2 + responseByteArray.length;
		IoBuffer responseIoBuffer = IoBuffer.allocate(returnPacketLength);
		System.out.println("返回数据的长度 = " + returnPacketLength);
		// 1.size
		responseIoBuffer.put(DataTypeTranslater.intToByte(returnPacketLength));
		// 2.ProtoHead
		System.out.println("返回类型号 = " + messageType.KeepAliveSync.getNumber() + "   名称 = " + ProtoHead.ENetworkMessage.valueOf(messageType.KeepAliveSync.getNumber()).toString());
		responseIoBuffer.put(DataTypeTranslater.intToByte(messageType.KeepAliveSync.getNumber()));
		// 3.Message
		System.out.println("返回的byte[] :" + responseByteArray);
		responseIoBuffer.put(responseByteArray);
		responseIoBuffer.flip();
		session.write(responseIoBuffer);
		System.out.println("发送完毕！！");
	}

	// 由底层决定是否创建一个session
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		System.out.println("sessionCreated");
	}

	// 创建了session 后会回调sessionOpened
	public void sessionOpened(IoSession session) throws Exception {
		count++;
		System.out.println("\n第 " + count + " 个 client 登陆！address： : " + session.getRemoteAddress());
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println("sessionClosed");

	}
}