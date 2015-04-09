package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import exception.NoIpException;
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

	Logger logger = Logger.getLogger(ServerNetwork.class);

	private InetSocketAddress inetSocketAddress;
	private IoAcceptor acceptor;

	private ServerNetwork() {
	}

	/**
	 * 初始化
	 * 
	 * @throws IOException
	 * @author Feng
	 */
	public void init() throws IOException {
		// 显示IP地址
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			Debug.log("IP address:" + addr.getHostAddress().toString());
			Debug.log("Host Name:" + addr.getHostName().toString());
			// logger.debug("IP地址:"+addr.getHostAddress().toString());
			// logger.debug("本机名称:"+ addr.getHostName().toString());

		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		Debug.log("Port Number：8081");
		// logger.debug("端口号：8081");

		acceptor = new NioSocketAcceptor();
		// 指定编码解码器
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MinaEncoder(), new MinaDecoder()));
		acceptor.setHandler(this);
		acceptor.bind(new InetSocketAddress(8081));
	}

	public void onDestroy() {
		acceptor.unbind(inetSocketAddress);
	}

	private int count = 0;

	/**
	 * 接收到新的数据
	 * 
	 * @author Feng
	 */
	@Override
	public void messageReceived(IoSession ioSession, Object message) throws Exception {
		// 接收客户端的数据
		IoBuffer ioBuffer = (IoBuffer) message;
		byte[] byteArray = new byte[ioBuffer.limit()];
		ioBuffer.get(byteArray, 0, ioBuffer.limit());

//		Debug.log("byteArray.length = " + byteArray.length);
//		dealRequest(session, byteArray);
		ClientRequest_Dispatcher.instance.dispatcher(new NetworkMessage(ioSession, byteArray));
		
//		// 大小
//		int size;
//		// 分割数据进行单独请求的处理
//		byte[] oneReqBytes;
//		int reqOffset = 0;
//		do {
//			Debug.log("\nServerNetwork: Start cut a new Request from Client!");
//			size = DataTypeTranslater.bytesToInt(byteArray, reqOffset);
//			System.out.println("size:" + size);
//			if (size == 0)
//				break;
//			oneReqBytes = new byte[size];
//			for (int i = 0; i < size; i++)
//				oneReqBytes[i] = byteArray[reqOffset + i];
//
//			dealRequest(session, size, oneReqBytes);
//
//			reqOffset += size;
//		} while (reqOffset < byteArray.length);

		// new Thread(new check(session)).start();

	}

	/**
	 * 用于处理一个请求
	 * 
	 * @param session
	 * @param size
	 * @param byteArray
	 * @author Feng
	 */
//	private void dealRequest(IoSession ioSession, byte[] byteArray) {
//		try {
//			ServerModel.instance.addClientRequestToQueue(ioSession, byteArray);
//			Debug.log("ServerNetwork", "Put Client's(" + ServerModel.getIoSessionKey(ioSession) + ") Request(size="
//					+ byteArray.length + ")into Queue!");
//		} catch (InterruptedException e) {
//			Debug.log(Debug.LogType.FAULT, "ServerNetwork", "Put client request into queue fail!\n" + e.toString());
//			System.err.println("ServerNetwork : 往请求队列中添加请求事件异常!");
//			e.printStackTrace();
//		} catch (NoIpException e) {
//			Debug.log(Debug.LogType.FAULT, "ServerNetwork", "Put client request into queue fail!\n" + e.toString());
//			e.printStackTrace();
//		}
//	}

	/**
	 * 由底层决定是否创建一个session
	 * 
	 * @author Feng
	 */
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		Debug.log("sessionCreated");
	}

	/**
	 * 创建了session 后会回调sessionOpened
	 * 
	 * @author Feng
	 */
	public void sessionOpened(IoSession session) throws Exception {
		count++;
		Debug.log("\n The " + count + " client connected! address : " + session.getRemoteAddress());
		Debug.log("ServerNetwork", "find a Client connected,save into table");
		addClientUserToTable(session);
	}

	/**
	 * 发送成功后会回调的方法
	 * 
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
	 * session 空闲的时候调用
	 * 
	 * @author Feng
	 */
	public void sessionIdle(IoSession session, IdleStatus status) {
		Debug.log("connect idle");
	}

	/**
	 * 异常捕捉
	 * 
	 * @author Feng
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		Debug.log("throws exception");
		Debug.log("session.toString()", session.toString());
		Debug.log("cause.toString()", cause.toString());
		Debug.log("Report Error Over!!");
	}

	/**
	 * 将新的用户添加到“已连接用户信息表”中
	 * 
	 * @param ioSession
	 * @author Feng
	 */
	public void addClientUserToTable(IoSession ioSession) {
		// 已有就不加进来了
		if (ServerModel.instance.getClientUserFromTable(ioSession.getRemoteAddress().toString()) != null) {
			Debug.log(Debug.LogType.ERROR, "User exist when Save user into Table!");
			System.err.println("添加时用户已存在");
			return;
		}

		Debug.log("ServerNetwork", "Find new User(" + ioSession.getRemoteAddress() + ") connected,save into table");
		try {
			ServerModel.instance.addClientUserToTable(ioSession, new ClientUser(ioSession));
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 给客户端发包
	 * 
	 * @param ioSession
	 * @param byteArray
	 * @author Feng
	 */
	public void sendMessageToClient(IoSession ioSession, byte[] byteArray) {
		IoBuffer responseIoBuffer = IoBuffer.allocate(byteArray.length);
		responseIoBuffer.put(byteArray);
		responseIoBuffer.flip();
		Debug.log(new String[] { "ServerNetwork", "sendMessageToClient" },
				"Send packet(" + NetworkMessage.getMessageType(byteArray).toString() + ") to client!");
		ioSession.write(responseIoBuffer);
	}
}
