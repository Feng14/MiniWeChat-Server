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

import exception.NoIpException;

import protocol.ProtoHead;
import tools.DataTypeTranslater;
import tools.Debug;

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
	 * @author Feng
	 */
	public void init() throws IOException {
		// 显示IP地址
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			Debug.log("IP地址", addr.getHostAddress().toString());
			Debug.log("本机名称", addr.getHostName().toString());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		Debug.log("端口号：8080");

		IoAcceptor acceptor = new NioSocketAcceptor();
		acceptor.setHandler(this);
		acceptor.bind(new InetSocketAddress(8080));
	}

	private int count = 0;

	/**
	 *  接收到新的数据
	 * @author Feng
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// 接收客户端的数据
		IoBuffer ioBuffer = (IoBuffer) message;
		byte[] byteArray = new byte[ioBuffer.limit()];
		ioBuffer.get(byteArray, 0, ioBuffer.limit());

		Debug.log("byteArray.length = " + byteArray.length);
		// 大小
		int size;
		// 分割数据进行单独请求的处理
		byte[] oneReqBytes;
		int reqOffset = 0;
		do {
			Debug.log("\nServerNetwork: 开始分割一个新的请求!");
			size = DataTypeTranslater.bytesToInt(byteArray, reqOffset);
			System.out.println("size:" + size);
			if (size == 0)
				break;
			oneReqBytes = new byte[size];
			for (int i = 0; i < size; i++)
				oneReqBytes[i] = byteArray[reqOffset + i];

			dealRequest(session, size, oneReqBytes);

			reqOffset += size;
		} while (reqOffset < byteArray.length);

//		new Thread(new check(session)).start();

	}
	
	/**
	 *  用于处理一个请求
	 * @param session
	 * @param size
	 * @param byteArray
	 * @author Feng
	 */
	private void dealRequest(IoSession ioSession, int size, byte[] byteArray) {
		try {
			ServerModel.instance.addClientRequestToQueue(ioSession, byteArray);
			Debug.log("ServerNetwork", "将Client请求放入待处理队列");
		} catch (InterruptedException e) {
			System.err.println("ServerNetwork : 往请求队列中添加请求事件异常!");
			e.printStackTrace();
		}
	}

	/**
	 *  由底层决定是否创建一个session
	 * @author Feng
	 */
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		Debug.log("sessionCreated");
	}

	/**
	 *  创建了session 后会回调sessionOpened
	 * @author Feng
	 */
	public void sessionOpened(IoSession session) throws Exception {
		count++;
		Debug.log("\n第 " + count + " 个 client 登陆！address： : " + session.getRemoteAddress());
		Debug.log("ServerNetwork", "检测到一个Client的连接，添加进表中");
		addClientUserToTable(session);
	}

	/**
	 *  发送成功后会回调的方法
	 * @author Feng
	 */
	public void messageSent(IoSession session, Object message) {
		Debug.log("message send to client");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		Debug.log("sessionClosed");

	}

	/**
	 *  session 空闲的时候调用
	 * @author Feng
	 */
	public void sessionIdle(IoSession session, IdleStatus status) {
		Debug.log("connect idle");
	}

	/**
	 *  异常捕捉
	 * @author Feng
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		Debug.log("throws exception");
		Debug.log("session.toString()", session.toString());
		Debug.log("cause.toString()", cause.toString());
		Debug.log("报错完毕！！");
	}
	
	/**
	 * 将新的用户添加到“已连接用户信息表”中
	 * @param ioSession
	 * @author Feng
	 */
	public void addClientUserToTable(IoSession ioSession){
		// 已有就不加进来了
		if (ServerModel.instance.getClientUserFromTable(ioSession.getRemoteAddress().toString()) != null){
			System.err.println("添加时用户已存在");
			return;
		}
		
		Debug.log("ServerNetwork", "发现新的用户" + ioSession.getRemoteAddress() + "连接，加入用户表");
		try {
			ServerModel.instance.addClientUserToTable(ioSession, new ClientUser(ioSession));
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 给客户端发包
	 * @param ioSession
	 * @param byteArray
	 * @author Feng
	 */
	public void sendMessageToClient(IoSession ioSession, byte[] byteArray) {
		IoBuffer responseIoBuffer = IoBuffer.allocate(byteArray.length);
		responseIoBuffer.put(byteArray);
		responseIoBuffer.flip();
		ioSession.write(responseIoBuffer);
	}
}
