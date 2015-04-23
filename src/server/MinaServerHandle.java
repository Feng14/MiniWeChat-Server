package server;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import exception.NoIpException;

import tools.Debug;

public class MinaServerHandle extends IoHandlerAdapter {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private ServerModel serverModel;
	private ClientRequest_Dispatcher clientRequest_Dispatcher;

	
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

	/**
	 * 接收到新的数据
	 * 
	 * @author Feng
	 */
	@Override
	public void messageReceived(IoSession ioSession, Object message) {
		logger.info("received");
		// 接收客户端的数据
		// IoBuffer ioBuffer = (IoBuffer) message;
		// byte[] byteArray = new byte[ioBuffer.limit()];
		// ioBuffer.get(byteArray, 0, ioBuffer.limit());

		NetworkPacket packetFromClient = (NetworkPacket) message;
		Debug.log("byteArray.length = " + packetFromClient.getMessageLength());
		// System.out.println(DataTypeTranslater.bytesToInt(byteArray, 0));
		// dealRequest(session, byteArray);
		// for (byte b : packetFromClient.arrayBytes)
		// System.out.println(b);
		clientRequest_Dispatcher.dispatcher(packetFromClient);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		Debug.log("sessionClosed");

	}

	/**
	 * 异常捕捉
	 * 
	 * @author Feng
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		if (cause.toString().equals("java.io.IOException: Connection reset by peer"))
			return;
		logger.error("throws exception");
		logger.error("session.toString() : " + session.toString());
		logger.error("cause.toString() : " + cause.toString());
		String exceptionStack = "";
		for (StackTraceElement element : cause.getStackTrace())
			exceptionStack += element.toString() + "\n";
		logger.error("stack : " + exceptionStack);
		logger.error("Report Error Over!!");
	}

	/**
	 * 由底层决定是否创建一个session
	 * 
	 * @author Feng
	 */
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
		Debug.log("sessionCreated");
	}

	/**
	 * 创建了session 后会回调sessionOpened
	 * 
	 * @author Feng
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
//		count++;
//		Debug.log("The " + count + " client connected! address : " + session.getRemoteAddress());
		Debug.log("New client connected! address : " + session.getRemoteAddress());
		addClientUserToTable(session);
	}

	/**
	 * session 空闲的时候调用
	 * 
	 * @author Feng
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		super.sessionIdle(session, status);
		Debug.log("connect idle");
	}

	/**
	 * 发送成功后会回调的方法
	 * 
	 * @author Feng
	 */
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		super.messageSent(session, message);
		try {
			logger.info("Send Packet(" + NetworkPacket.getMessageType((byte[])message).name() + ") to Client!");
		} catch (Exception e) {
		}
//		Debug.log("message send to client");
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
			logger.error("添加时用户已存在");
			return;
		}

		Debug.log("ServerNetwork", "Find new User(" + ioSession.getRemoteAddress() + ") connected,save into table");
		try {
			serverModel.addClientUserToTable(ioSession, new ClientUser(ioSession));
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}
}

