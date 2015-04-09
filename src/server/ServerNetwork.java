package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
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
	// 轮询"等待client回复"的超时时间
	public static final long WAIT_CLIENT_RESPONSE_TIMEOUT = 3000;
	// 轮询"等待client回复"的重发次数
	public static final long WAIT_CLIENT_RESPONSE_TIMES = 3;
	
	// public static ServerNetwork instance = new ServerNetwork();
	private ServerModel serverModel;
	private ClientRequest_Dispatcher clientRequest_Dispatcher;

	Logger logger = Logger.getLogger(ServerNetwork.class);

	private InetSocketAddress inetSocketAddress;
	private IoAcceptor acceptor;

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
		acceptor.getSessionConfig().setMaxReadBufferSize(1024 * 8);
		acceptor.setHandler(this);
		acceptor.bind(new InetSocketAddress(8081));
	}

	public void onDestroy() {
		acceptor.unbind(inetSocketAddress);
	}

	public ServerModel getServerModel() {
		return serverModel;
	}

	public void setServerModel(ServerModel serverModel) {
		this.serverModel = serverModel;
	}

	public ClientRequest_Dispatcher getClientRequest_Dispatcher() {
		return clientRequest_Dispatcher;
	}

	public void setClientRequest_Dispatcher(ClientRequest_Dispatcher clientRequest_Dispatcher) {
		this.clientRequest_Dispatcher = clientRequest_Dispatcher;
	}

	private int count = 0;

	/**
	 * 接收到新的数据
	 * 
	 * @author Feng
	 */
	@Override
	public void messageReceived(IoSession ioSession, Object message) {
		System.out.println("received");
		// 接收客户端的数据
		// IoBuffer ioBuffer = (IoBuffer) message;
		// byte[] byteArray = new byte[ioBuffer.limit()];
		// ioBuffer.get(byteArray, 0, ioBuffer.limit());

		PacketFromClient packetFromClient = (PacketFromClient) message;
		Debug.log("byteArray.length = " + packetFromClient.getMessageLength());
		// System.out.println(DataTypeTranslater.bytesToInt(byteArray, 0));
		// dealRequest(session, byteArray);
//		for (byte b : packetFromClient.arrayBytes)
//			System.out.println(b);
		clientRequest_Dispatcher.dispatcher(packetFromClient);

		// // 大小
		// int size;
		// // 分割数据进行单独请求的处理
		// byte[] oneReqBytes;
		// int reqOffset = 0;
		// do {
		// Debug.log("\nServerNetwork: Start cut a new Request from Client!");
		// size = DataTypeTranslater.bytesToInt(byteArray, reqOffset);
		// System.out.println("size:" + size);
		// if (size == 0)
		// break;
		// oneReqBytes = new byte[size];
		// for (int i = 0; i < size; i++)
		// oneReqBytes[i] = byteArray[reqOffset + i];
		//
		// dealRequest(session, size, oneReqBytes);
		//
		// reqOffset += size;
		// } while (reqOffset < byteArray.length);

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
	// private void dealRequest(IoSession ioSession, byte[] byteArray) {
	// try {
	// ServerModel.instance.addClientRequestToQueue(ioSession, byteArray);
	// Debug.log("ServerNetwork", "Put Client's(" +
	// ServerModel.getIoSessionKey(ioSession) + ") Request(size="
	// + byteArray.length + ")into Queue!");
	// } catch (InterruptedException e) {
	// Debug.log(Debug.LogType.FAULT, "ServerNetwork",
	// "Put client request into queue fail!\n" + e.toString());
	// System.err.println("ServerNetwork : 往请求队列中添加请求事件异常!");
	// e.printStackTrace();
	// } catch (NoIpException e) {
	// Debug.log(Debug.LogType.FAULT, "ServerNetwork",
	// "Put client request into queue fail!\n" + e.toString());
	// e.printStackTrace();
	// }
	// }

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
		Debug.log("The " + count + " client connected! address : " + session.getRemoteAddress());
		// Debug.log("ServerNetwork",
		// "Find a Client connected,save into table");
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
		if (serverModel.getClientUserFromTable(ioSession.getRemoteAddress().toString()) != null) {
			Debug.log(Debug.LogType.ERROR, "User exist when Save user into Table!");
			System.err.println("添加时用户已存在");
			return;
		}

		Debug.log("ServerNetwork", "Find new User(" + ioSession.getRemoteAddress() + ") connected,save into table");
		try {
			serverModel.addClientUserToTable(ioSession, new ClientUser(ioSession));
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * 添加一个等待客户端回复的监听（服务器向客户端发送消息后，要求客户端回复）
	 * 
	 * @param ioSession
	 * @param key
	 * @param messageHasSentww
	 * @author Feng
	 */
	public void sendToClient(final WaitClientResponse waitClientResponse) {
		sendToClient(waitClientResponse, 0);
	}

	private void sendToClient(final WaitClientResponse waitClientResponse, final int times) {
		try {
			if (serverModel.getClientUserFromTable(waitClientResponse.ioSession) == null) {
				// 用户已掉线， 调用删除前的回调，然后删除
				if (waitClientResponse.waitClientResponseCallBack != null)
					waitClientResponse.waitClientResponseCallBack.beforeDelete();
				return;
			}
			// 用户在线，重发
			WriteFuture writeFuture = waitClientResponse.ioSession.write(waitClientResponse.packetFromServer);
			writeFuture.awaitUninterruptibly(WAIT_CLIENT_RESPONSE_TIMEOUT);
			writeFuture.addListener(new IoFutureListener<IoFuture>() {
				@Override
				public void operationComplete(IoFuture future) {
					if (((WriteFuture) future).isWritten())
						return;
					else {
						try {
							Debug.log("ServerModel", "Wait for Client(" + serverModel.getIoSessionKey(waitClientResponse.ioSession) + ") response timeout!");

							if (times < WAIT_CLIENT_RESPONSE_TIMES) {
								// 小于重发极限次数，重发
								Debug.log("ServerModel", "Client(" + serverModel.getIoSessionKey(waitClientResponse.ioSession) + ") online,send again!");
								sendToClient(waitClientResponse, times + 1);
							} else {
								// 大于重发极限次数，抛弃
								Debug.log("To many times, abandon!");
								return;
							}
						} catch (NoIpException e) {
							e.printStackTrace();
						}

					}

				}
			});
		} catch (NoIpException e) {
			e.printStackTrace();
			if (waitClientResponse.waitClientResponseCallBack != null)
				waitClientResponse.waitClientResponseCallBack.beforeDelete();
			return;
		}
	}

	/**
	 * 给客户端发包
	 * 
	 * @param ioSession
	 * @param byteArray
	 * @author Feng
	 */
//	public void sendMessageToClient(IoSession ioSession, byte[] byteArray) {
//		IoBuffer responseIoBuffer = IoBuffer.allocate(byteArray.length);
//		responseIoBuffer.put(byteArray);
//		responseIoBuffer.flip();
//		Debug.log(new String[] { "ServerNetwork", "sendMessageToClient" },
//				"Send packet(" + PacketFromClient.getMessageType(byteArray).toString() + ") to client!");
//		ioSession.write(responseIoBuffer);
//	}
	
}
