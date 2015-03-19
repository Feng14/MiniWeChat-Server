package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;
import tools.DataTypeTranslater;

/**
 * 服务器的网络层，负责网络交互
 * 
 * @author Feng
 * 
 */
public class ServerNetwork extends IoHandlerAdapter {
	public static ServerNetwork instance = new ServerNetwork();

	private ServerNetwork() {

	}

	/**
	 *  初始化
	 * @throws IOException
	 */
	public void init() throws IOException {
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
		acceptor.setHandler(this);
		acceptor.bind(new InetSocketAddress(8080));
	}

	private int count = 0;

	/**
	 *  接收到新的数据
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// 接收客户端的数据
		IoBuffer ioBuffer = (IoBuffer) message;
		byte[] byteArray = new byte[ioBuffer.limit()];
		ioBuffer.get(byteArray, 0, ioBuffer.limit());
		// System.out.println("messageReceived:" + new String(byteArray,
		// "UTF-8"));

		System.out.println("byteArray.length = " + byteArray.length);
		// 大小
		int size;
		// System.out.println("前四个byte为：");
		// for (int i=0; i<4; i++)
		// System.err.println(byteArray[i]);

		// 分割数据进行单独请求的处理
		byte[] oneReqBytes;
		int reqOffset = 0;
		do {
			System.out.println();
			System.out.println("开始处理一个新的请求!");
			size = DataTypeTranslater.bytesToInt(byteArray, 0);
			oneReqBytes = new byte[size];
			for (int i = 0; i < size; i++)
				oneReqBytes[i] = byteArray[reqOffset + i];

			dealRequest(session, size, oneReqBytes);

			reqOffset += size;
		} while (reqOffset < byteArray.length);

//		new Thread(new check(session)).start();

	}
	class check implements Runnable {
		IoSession mySession;
		public check(IoSession session) {
			mySession = session;
		}
		@Override
		public void run() {
			try {
				Scanner s = new Scanner(System.in);
				byte[] chechStr = new String("check online").getBytes();
				while (true) {
					System.err.println("shit");
					String string = s.next();
//				if (s.equals("s")){
					System.err.println("input " + string);
					IoBuffer responseIoBuffer = IoBuffer.allocate(chechStr.length);
					responseIoBuffer.put(chechStr);
					responseIoBuffer.flip();
					mySession.write(chechStr);
					System.err.println("发送校验");
//				}
				}
			} catch (Exception e) {
				System.err.println("my Exception");
				// TODO: handle exception
			}
		}
	}

	/**
	 *  用于处理一个请求
	 * @param session
	 * @param size
	 * @param byteArray
	 */
	private void dealRequest(IoSession session, int size, byte[] byteArray) {
		try {
			ServerModel.instance.requestQueue.put(new NetworkMessage(session, byteArray));
		} catch (InterruptedException e) {
			System.err.println("往请求队列中添加请求事件异常!");
			e.printStackTrace();
		}
		
//		System.out.println("Size is :" + size);
//		int offset = NetworkMessage.HEAD_INT_SIZE;
//
//		// 类型
//		int typeInt = DataTypeTranslater.bytesToInt(byteArray, 4);
//		// System.out.println("Type Number is " + typeInt);
//
//		ProtoHead.ENetworkMessage messageType = ProtoHead.ENetworkMessage.valueOf(typeInt);
//		System.out.println("Type is :" + messageType.toString());
//		offset += NetworkMessage.HEAD_INT_SIZE;
//
//		// 内容
//		byte[] messageBytes = new byte[size - NetworkMessage.HEAD_INT_SIZE * 2];
//		for (int i = 0; i < messageBytes.length; i++)
//			messageBytes[i] = byteArray[offset + i];
//
//		byte[] responseByteArray = new byte[0];
//		// 反序列化
//		try {
//			KeepAliveMsg.KeepAliveSyncPacket packet = KeepAliveMsg.KeepAliveSyncPacket.parseFrom(messageBytes);
//			System.out.println("收到包：" + "   " + messageType.toString() + "  " + packet.getA() + "   " + packet.getB() + "   "
//					+ packet.getC());
//
//			// 重建--》回复
//			KeepAliveMsg.KeepAliveSyncPacket.Builder keepAliveSyncBuilder = KeepAliveMsg.KeepAliveSyncPacket.newBuilder();
//			int i = messageType.LoginRsp.getNumber();
//			keepAliveSyncBuilder.setA(12345678);
//			keepAliveSyncBuilder.setB(!packet.getB());
//			keepAliveSyncBuilder.setC("Holy Shit！！！");
//			responseByteArray = keepAliveSyncBuilder.build().toByteArray();
//
//			// 发送到客户端
//			int returnPacketLength = NetworkMessage.HEAD_INT_SIZE * 2 + responseByteArray.length;
//			IoBuffer responseIoBuffer = IoBuffer.allocate(returnPacketLength);
//			System.out.println("返回数据的长度 = " + returnPacketLength);
//			// 1.size
//			responseIoBuffer.put(DataTypeTranslater.intToByte(returnPacketLength));
//			// 2.ProtoHead
//			System.out.println("返回类型号 = " + messageType.KeepAliveSync.getNumber() + "   名称 = "
//					+ ProtoHead.ENetworkMessage.valueOf(messageType.KeepAliveSync.getNumber()).toString());
//			responseIoBuffer.put(DataTypeTranslater.intToByte(messageType.KeepAliveSync.getNumber()));
//			// 3.Message
//			System.out.println("返回的byte[] :" + responseByteArray);
//			responseIoBuffer.put(responseByteArray);
//
//			// for (int i=0; i<9; i++){
//			// responseIoBuffer.put(DataTypeTranslater.intToByte(returnPacketLength));
//			// responseIoBuffer.put(DataTypeTranslater.intToByte(messageType.KeepAliveSync.getNumber()));
//			// responseIoBuffer.put(responseByteArray);
//			// }
//			responseIoBuffer.flip();
//			session.write(responseIoBuffer);
//			session.write(responseIoBuffer);
//
//			System.out.println("发送完毕！！");
//			System.out.println("请求处理完毕");
//		} catch (Exception e) {
//			System.err.println(e.getMessage());
//		}
	}

	/**
	 *  由底层决定是否创建一个session
	 */
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		System.out.println("sessionCreated");
	}

	// 创建了session 后会回调sessionOpened
	public void sessionOpened(IoSession session) throws Exception {
		count++;
		System.out.println("\n第 " + count + " 个 client 登陆！address： : " + session.getRemoteAddress());
	}

	// 发送成功后会回调的方法
	public void messageSent(IoSession session, Object message) {
		System.out.println("message send to client");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println("sessionClosed");

	}

	// session 空闲的时候调用
	public void sessionIdle(IoSession session, IdleStatus status) {
		System.out.println("connect idle");
	}

	// 异常捕捉
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		System.out.println("throws exception");
		System.err.println("sesson.toString() :" + session.toString());
		System.err.println("cause.toString() :" + cause.toString());
		System.err.println("报错完毕！！");
	}
}
