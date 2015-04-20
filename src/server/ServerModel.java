package server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Observable;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import exception.NoIpException;
import protocol.ProtoHead;
import protocol.Msg.KeepAliveMsg;
import tools.DataTypeTranslater;
import tools.Debug;

/**
 * 网络逻辑层
 * 
 * @author Feng
 * 
 */
public class ServerModel extends Observable {
	private ServerModel serverModel;
	private ServerNetwork serverNetwork;
	Logger logger = Logger.getLogger(this.getClass());

	// 心跳包间隔(5秒)
	public static final int KEEP_ALIVE_PACKET_TIME = 5000;
	// 轮询"等待client回复"列表（waitClientRepTable）的间隔
	public static final int CHECK_WAIT_CLIENT_RESPONSE_DELTA_TIME = 1000;

	// public static ServerModel instance = new ServerModel();
	// Client请求队列
	// private LinkedBlockingQueue<NetworkMessage> requestQueue = new
	// LinkedBlockingQueue<NetworkMessage>();
	// 已连接用户信息表(Key 为IoSession.getRemoteAddress().toString)
	private Hashtable<String, ClientUser> clientUserTable = new Hashtable<String, ClientUser>();

	// 监听客户端回复的表
	// private Hashtable<String, WaitClientResponse> waitClientRepTable = new
	// Hashtable<String, WaitClientResponse>();
	
	public ServerModel(){
		init();
	}

	public ServerModel getServerModel() {
		return serverModel;
	}

	public void setServerModel(ServerModel serverModel) {
		this.serverModel = serverModel;
	}

	public ServerNetwork getServerNetwork() {
		return serverNetwork;
	}

	public void setServerNetwork(ServerNetwork serverNetwork) {
		this.serverNetwork = serverNetwork;
	}

	/**
	 * 创建一个随机的MessageId
	 * 
	 * @author Feng
	 * @return
	 */
	public static byte[] createMessageId() {
		return DataTypeTranslater.floatToBytes((float) Math.random());
	}

	/**
	 * 初始化
	 * 
	 * @author Feng
	 */
	public void init() {
		// 开始新线程
		// new Thread(new DealClientRequest()).start();
		new Thread(new KeepAlivePacketSenser()).start();
		// new Thread(new CheckWaitClientResponseThread()).start();
	}

	/**
	 * 往客户端请求列表中加入一条请求
	 * 
	 * @param ioSession
	 * @param arrayBytes
	 * @author Feng
	 * @throws InterruptedException
	 */
	// public void addClientRequestToQueue(IoSession ioSession, byte[]
	// byteArray) throws InterruptedException {
	// requestQueue.put(new NetworkMessage(ioSession, byteArray));
	// }

	/**
	 * 往“已连接用户信息表”中添加一个新用户
	 * 
	 * @param key
	 * @param clientUser
	 * @author Feng
	 * @throws NoIpException
	 */
	public void addClientUserToTable(IoSession ioSession, ClientUser clientUser) throws NoIpException {
		synchronized (clientUserTable) {
			clientUser.onLine = true;
			clientUserTable.put(getIoSessionKey(ioSession), clientUser);
		}
	}

	/**
	 * 从iosession生成Key
	 * 
	 * @param ioSession
	 * @return
	 * @throws NoIpException
	 */
	public static String getIoSessionKey(IoSession ioSession) throws NoIpException {
		// System.err.println("1.2  " + (ioSession.getRemoteAddress() == null));
		if (ioSession.getRemoteAddress() == null)
			throw new NoIpException();
		return ((InetSocketAddress) ioSession.getRemoteAddress()).getAddress().toString() + ":"
				+ ((InetSocketAddress) ioSession.getRemoteAddress()).getPort();
	}

	/**
	 * 从“已连接用户信息表”中获取用户
	 * 
	 * @param key
	 * @return ClientUser
	 * @author Feng
	 */
	public ClientUser getClientUserFromTable(String key) {
		synchronized (clientUserTable) {
			return clientUserTable.get(key);
		}
	}

	public ClientUser getClientUserFromTable(IoSession ioSession) throws NoIpException {
		synchronized (clientUserTable) {
			return getClientUserFromTable(getIoSessionKey(ioSession));
		}
	}

	/**
	 * 根据userId从“已连接用户信息表”中获取用户
	 * 
	 * @param userId
	 * @return
	 */
	public ClientUser getClientUserByUserId(String userId) {
		Iterator iterator = clientUserTable.keySet().iterator();
		String key;
		ClientUser user;

		synchronized (clientUserTable) {
			while (iterator.hasNext()) {

				key = iterator.next().toString();

				if (!clientUserTable.containsKey(key))
					continue;

				user = clientUserTable.get(key);

				if (user.userId == null)
					continue;

				if (user.userId.equals(userId))
					return user;
			}
		}
		return null;
	}

	/**
	 * 从在线用户信息表删除一个用户
	 * 
	 * @param key
	 */
	public void removeClientUserFromTable(String key) {
		synchronized (clientUserTable) {
			clientUserTable.remove(key);
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
	// public void addClientResponseListener(IoSession ioSession, byte[] key,
	// byte[] messageHasSent,
	// WaitClientResponseCallBack waitClientResponseCallBack) {
	// WaitClientResponse waitClientResponse = new WaitClientResponse(ioSession,
	// messageHasSent, waitClientResponseCallBack);
	// waitClientResponse.time = new Date().getTime();
	// // 加入到“等待回复表”中，由CheckWaitClientResponseThread 线程进行轮询
	// System.err.println("add Listener, key: " + key.toString());
	// waitClientRepTable.put(key.toString(), waitClientResponse);
	// }

	/**
	 * 删除一个等待客户端回复的监听（服务器向客户端发送消息后，要求客户端回复）
	 * 
	 * @param ioSession
	 * @param key
	 * @param messageHasSent
	 * @author Feng
	 */
	// public void removeClientResponseListener(byte[] key) {
	// synchronized (waitClientRepTable) {
	// waitClientRepTable.remove(key);
	// }
	// }

	/**
	 * 查找一个等待客户端回复的监听（服务器向客户端发送消息后，要求客户端回复）
	 * 
	 * @param ioSession
	 * @param key
	 * @param messageHasSent
	 * @author Feng
	 */
	// public WaitClientResponse getClientResponseListener(byte[] key) {
	// synchronized (waitClientRepTable) {
	// return waitClientRepTable.get(key);
	// }
	// }

	/**
	 * 广播前的设置变更
	 */
	public void setChange() {
		super.setChanged();
	}

	public void notify(Object obj) {
		setChange();
		notifyObservers(this);
	}

	/**
	 * 用于处理用户请求的线程
	 * 
	 * @author Feng
	 * 
	 */
	// private class DealClientRequest implements Runnable {
	// @Override
	// public void run() {
	// NetworkMessage networkMessage = null;
	// // 循环获取新的请求，阻塞式
	// while (true) {
	// try {
	// networkMessage = requestQueue.take();
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// Debug.log("ServerModel",
	// "'ServerModel' get a request from Client,now transmit to 'ClientRequest_Dispatcher'!");
	// if (networkMessage == null)
	// continue;
	// ClientRequest_Dispatcher.instance.dispatcher(networkMessage);
	//
	// }
	// }
	// }

	/**
	 * 用于定时发送心跳包
	 * 
	 * @author Feng
	 * 
	 */
	private class KeepAlivePacketSenser implements Runnable, IoFutureListener<IoFuture> {
		private Iterator iterator;
		private String key;

		@Override
		public void run() {
			KeepAliveMsg.KeepAliveSyncPacket.Builder packet = KeepAliveMsg.KeepAliveSyncPacket.newBuilder();
			byte[] packetBytes = packet.build().toByteArray();
			// 创建心跳包
			PacketFromServer packetWillSend;

			IoBuffer responseIoBuffer;
			ArrayList<String> keyIterators;

			while (true) {
				try {
					Thread.sleep(KEEP_ALIVE_PACKET_TIME);

					ClientUser user;
					iterator = clientUserTable.keySet().iterator();

//					Debug.log("ServerModel", "Start a new round of sending 'KeepAlivePacket'! " + clientUserTable.size()
//							+ " user exist!");
//					logger.info("ServerModel Start a new round of sending 'KeepAlivePacket'! " + clientUserTable.size()
//							+ " user exist!");
					synchronized (clientUserTable) {
						while (iterator.hasNext()) {
							// for (String key : keyIterators) {
							// Debug.log("ServerModel", "进入发心跳包循环!");

							key = iterator.next().toString();

							if (!clientUserTable.containsKey(key))
								continue;
							user = clientUserTable.get(key);

							// 将上次没有回复的干掉，从用户表中删掉
							// if (user.onLine == false) {
							// Debug.log("ServerModel", "Client User(" +
							// user.ioSession.getRemoteAddress()
							// + ") was offline,now delete it!");
							// user.ioSession.close(false);
							// iterator.remove();
							// continue;
							// }

							// 创建要发送的包
							packetWillSend = new PacketFromServer(ProtoHead.ENetworkMessage.KEEP_ALIVE_SYNC.getNumber(),
									packetBytes);

							// 发送心跳包之前先将online设为False表示不在线，若是Client回复，则重新设为True
							// ，表示在线
//							Debug.log("ServerModel", " Send 'KeepAlivePacket' to Client(" + user.ioSession.getRemoteAddress()
//									+ ")");
//							logger.info("ServerModel  Send 'KeepAlivePacket' to Client(" + user.ioSession.getRemoteAddress()
//									+ ")");

							WriteFuture writeFuture = user.ioSession.write(packetWillSend);
							writeFuture.addListener(this);
						}
					}
				} catch (InterruptedException e) {
//					Debug.log(Debug.LogType.FAULT, "'Send KeepAlivePacket Thread' fail at sleep module!\n" + e.toString());
					logger.info("'Send KeepAlivePacket Thread' fail at sleep module!\n" + e.toString());
					System.err.println("发行心跳包线程异常! -----睡眠模块");
					e.printStackTrace();
				}
			}
		}

		@Override
		public void operationComplete(IoFuture future) {
			// 掉线处理
			if (((WriteFuture) future).isWritten()) {
				// Debug.log(new String[] { "ServerModel",
				// "KeepAlivePacketSenser" },
				// "User " + ServerModel.getIoSessionKey(user.ioSession) +
				// " still online!");
			} else {
				// 发送失败，判定掉线
				Debug.log("ServerModel", "Client User(" + key + ") was offline,now delete it!");
				iterator.remove();
			}
		}
	}

	/**
	 * 轮询"等待client回复"列表（waitClientRepTable），检查是否有超时的条目 超时的进行重发
	 * 
	 * @author Feng
	 * 
	 */
	// private class CheckWaitClientResponseThread implements Runnable {
	// @Override
	// public void run() {
	// long currentTime;
	// WaitClientResponse waitObj;
	// String key;
	// ClientUser clientUser = null;
	// while (true) {
	// currentTime = new java.util.Date().getTime();
	// // 每隔CHECK_WAIT_CLIENT_RESPONSE_DELTA_TIME时间轮询一次
	// try {
	// Thread.sleep(CHECK_WAIT_CLIENT_RESPONSE_DELTA_TIME);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// // 对每个用户进行检查
	// // Debug.log(new String[] { "ServerModel",
	// // "CheckWaitClientResponseThread" }, "开始检测等待客户端未回复列表，共 "
	// // + waitClientRepTable.size() + " 个等待!");
	// Iterator iterator = waitClientRepTable.keySet().iterator();
	// synchronized (waitClientRepTable) {
	// while (iterator.hasNext()) {
	// key = iterator.next().toString();
	// waitObj = waitClientRepTable.get(key);
	// // System.err.println("key : " + key + "  size: " +
	// // waitClientRepTable.size() + "  obj=null : " +
	// // (waitObj == null));
	// if (waitObj == null)
	// continue;
	//
	// // System.err.println(currentTime - waitObj.time);
	// if ((currentTime - waitObj.time) > WAIT_CLIENT_RESPONSE_TIMEOUT) {
	// // 超时，重发
	// Debug.log("ServerModel", "Wait for Client(" +
	// waitObj.ioSession.getRemoteAddress()
	// + ") response timeout!");
	// // 不在线,调用删前回调，删除
	// try {
	// clientUser =
	// clientUserTable.get(ServerModel.getIoSessionKey(waitObj.ioSession));
	// } catch (NoIpException e) {
	// // e.printStackTrace();
	// }
	// if (clientUser == null || !clientUser.onLine) {
	// Debug.log("ServerModel", "Client(" + waitObj.ioSession.getRemoteAddress()
	// + ") was offline,now delete it!");
	// if (waitObj.waitClientResponseCallBack != null)
	// waitObj.waitClientResponseCallBack.beforeDelete();
	// waitClientRepTable.remove(key);
	// continue;
	// }
	// // 重发，重置等待时间
	// Debug.log("ServerModel", "Client(" + waitObj.ioSession.getRemoteAddress()
	// + ") online,send again!");
	// serverNetwork.sendMessageToClient(waitObj.ioSession,
	// waitObj.messageHasSent);
	// waitObj.time = currentTime;
	// }
	// }
	// }
	// }
	// }
	// }
}
