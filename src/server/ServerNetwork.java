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

//		System.out.println("byteArray.length = " + byteArray.length);
		// 大小
		int size;
		// 分割数据进行单独请求的处理
		byte[] oneReqBytes;
		int reqOffset = 0;
		do {
			System.out.println("\nServerNetwork: 开始分割一个新的请求!");
			size = DataTypeTranslater.bytesToInt(byteArray, 0);
			oneReqBytes = new byte[size];
			for (int i = 0; i < size; i++)
				oneReqBytes[i] = byteArray[reqOffset + i];

			dealRequest(session, size, oneReqBytes);

			reqOffset += size;
		} while (reqOffset < byteArray.length);

//		new Thread(new check(session)).start();

	}
	
	/**
	 * 测试用
	 * @author Feng
	 *
	 */
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
			System.out.println("将Client请求放入待处理队列");
		} catch (InterruptedException e) {
			System.err.println("往请求队列中添加请求事件异常!");
			e.printStackTrace();
		}
	}

	/**
	 *  由底层决定是否创建一个session
	 */
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		System.out.println("sessionCreated");
	}

	/**
	 *  创建了session 后会回调sessionOpened
	 */
	public void sessionOpened(IoSession session) throws Exception {
		count++;
		System.out.println("\n第 " + count + " 个 client 登陆！address： : " + session.getRemoteAddress());
		System.out.println("ServerNetwork: 检测到一个Client的连接，添加进表中");
		addClientUserToTable(session);
	}

	/**
	 *  发送成功后会回调的方法
	 */
	public void messageSent(IoSession session, Object message) {
		System.out.println("message send to client");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println("sessionClosed");

	}

	/**
	 *  session 空闲的时候调用
	 */
	public void sessionIdle(IoSession session, IdleStatus status) {
		System.out.println("connect idle");
	}

	/**
	 *  异常捕捉
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		System.out.println("throws exception");
		System.err.println("sesson.toString() :" + session.toString());
		System.err.println("cause.toString() :" + cause.toString());
		System.err.println("报错完毕！！");
	}
	
	/**
	 * 将新的用户添加到“已连接用户信息表”中
	 * @param ioSession
	 */
	public void addClientUserToTable(IoSession ioSession){
		// 已有就不加进来了
		if (ServerModel.instance.clientUserTable.containsKey(ioSession.getRemoteAddress()))
			return;
		
		ServerModel.instance.clientUserTable.put(ioSession.getRemoteAddress().toString(), new ClientUser(ioSession));
	}
	
	/**
	 * 给客户端发包
	 * @param ioSession
	 * @param byteArray
	 */
	public void sendMessageToClient(IoSession ioSession, byte[] byteArray) {
		IoBuffer responseIoBuffer = IoBuffer.allocate(byteArray.length);
		responseIoBuffer.put(byteArray);
		responseIoBuffer.flip();
		ioSession.write(responseIoBuffer);
	}
}
