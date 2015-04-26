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
import org.springframework.context.support.ClassPathXmlApplicationContext;

import exception.NoIpException;
import tools.DataTypeTranslater;
import tools.Debug;

/**
 * 服务器的网络层，负责网络交互
 * 
 * @author Feng
 * 
 */
public class ServerNetwork {
	// 轮询"等待client回复"的超时时间
	public static final long WAIT_CLIENT_RESPONSE_TIMEOUT = 3000;
	// 轮询"等待client回复"的重发次数
	public static final long WAIT_CLIENT_RESPONSE_TIMES = 3;

	// public static ServerNetwork instance = new ServerNetwork();
	private ServerModel serverModel;
	private ClientRequest_Dispatcher clientRequest_Dispatcher;
	private MinaServerHandle minaServerHandle;
	private ProtocolCodecFilter protocolCodecFilter;
	private InetAddress addr;

	Logger logger = Logger.getLogger(ServerNetwork.class);

	private InetSocketAddress inetSocketAddress;
	private IoAcceptor acceptor;

	public ServerNetwork() {
	}

	/**
	 * 初始化
	 * 
	 * @throws IOException
	 * @author Feng
	 */
	public void init() {
		if (addr != null || minaServerHandle == null)
			return;

		// 显示IP地址
		try {
			addr = InetAddress.getLocalHost();
			logger.info("IP address:" + addr.getHostAddress().toString());
			logger.info("Host Name:" + addr.getHostName().toString());

		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		// Debug.log("Port Number：8081");
		logger.debug("Port Number：8081");

		acceptor = new NioSocketAcceptor();
		// 指定编码解码器
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MinaEncoder(), new MinaDecoder()));
		// System.out.println("codec " + (protocolCodecFilter == null));
		// acceptor.getFilterChain().addLast("codec", protocolCodecFilter);
		acceptor.getSessionConfig().setMaxReadBufferSize(1024 * 8);
		// System.out.println("minaServerHandle :" + (minaServerHandle ==
		// null));
		acceptor.setHandler(minaServerHandle);
		// acceptor.setHandler(new MinaServerHandle());
		try {
			acceptor.bind(new InetSocketAddress(8081));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("ServerNetwork : Acceptor bind Exception!;\n" + e.toString());
		}

		// new ClassPathXmlApplicationContext("trapReceiverContext.xml");
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

	public MinaServerHandle getMinaServerHandle() {
		return minaServerHandle;
	}

	public void setMinaServerHandle(MinaServerHandle minaServerHandle) {
		this.minaServerHandle = minaServerHandle;
		init();
	}

	public ProtocolCodecFilter getProtocolCodecFilter() {
		return protocolCodecFilter;
	}

	public void setProtocolCodecFilter(ProtocolCodecFilter protocolCodecFilter) {
		this.protocolCodecFilter = protocolCodecFilter;
	}

	/**
	 * 添加一个等待客户端回复的监听（服务器向客户端发送消息后，要求客户端回复）
	 * 
	 * @param ioSession
	 * @param key
	 * @param messageHasSentww
	 * @author Feng
	 */
	public void sendToClient(IoSession ioSession, PacketFromServer packetFromServer) {
		sendToClient(new WaitClientResponse(ioSession, packetFromServer));
	}

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
			// writeFuture.awaitUninterruptibly(WAIT_CLIENT_RESPONSE_TIMEOUT);
			writeFuture.addListener(new IoFutureListener<IoFuture>() {
				@Override
				public void operationComplete(IoFuture future) {
					if (((WriteFuture) future).isWritten())
						return;
					else {
						// Debug.log("ServerModel",
						// "Wait for Client(" +
						// serverModel.getIoSessionKey(waitClientResponse.ioSession)
						// + ") response timeout!");
						try {
							logger.info("ServerModel Wait for Client("
									+ serverModel.getIoSessionKey(waitClientResponse.ioSession) + ") response timeout!");

							if (times < WAIT_CLIENT_RESPONSE_TIMES) {
								// 小于重发极限次数，重发
								// Debug.log("ServerModel", "Client(" +
								// serverModel.getIoSessionKey(waitClientResponse.ioSession)
								// + ") online,send again!");
								logger.info("ServerModel Client(" + serverModel.getIoSessionKey(waitClientResponse.ioSession)
										+ ") online,send again!");
								sendToClient(waitClientResponse, times + 1);
							} else {
								// 大于重发极限次数，抛弃
								logger.info("To many times, abandon!");
								return;
							}
						} catch (Exception e) {
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
	 * Mina网络连接回调方法
	 * 
	 * @author Feng
	 * 
	 */
}
