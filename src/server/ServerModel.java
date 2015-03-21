package server;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.sun.org.apache.bcel.internal.generic.NEW;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;
import tools.DataTypeTranslater;
import tools.Debug;

/**
 * 网络逻辑层
 * 
 * @author Feng
 * 
 */
public class ServerModel {
	// 心跳包间隔(5秒)
	public static final int KEEP_ALIVE_PACKET_TIME = 5000;
	// 轮询"等待client回复"列表（waitClientRepTable）的间隔
	public static final int CHECK_WAIT_CLIENT_RESPONSE_DELTA_TIME = 1000;
	// 轮询"等待client回复"列表（waitClientRepTable）的超时时间
	public static final long WAIT_CLIENT_RESPONSE_TIMEOUT = 3000;

	public static ServerModel instance = new ServerModel();
	// Client请求队列
	private LinkedBlockingQueue<NetworkMessage> requestQueue = new LinkedBlockingQueue<NetworkMessage>();
	// 已连接用户信息表
	private Hashtable<String, ClientUser> clientUserTable = new Hashtable<String, ClientUser>();
	// 监听客户端回复的表
	private Hashtable<byte[], WaitClientResponse> waitClientRepTable = new Hashtable<byte[], WaitClientResponse>();

	private ServerModel() {

	}

	/**
	 * 创建一个随机的MessageId
	 * @author Feng
	 * @return
	 */
	public static byte[] createMessageId() {
		return DataTypeTranslater.floatToBytes((float) Math.random());
	}

	/**
	 * 初始化
	 * @author Feng
	 */
	public void init() {
		// 开始新线程
		new Thread(new DealClientRequest()).start();
		new Thread(new KeepAlivePacketSenser()).start();
		new Thread(new CheckWaitClientResponseThread()).start();
	}

	/**
	 *  往客户端请求列表中加入一条请求
	 * @param ioSession
	 * @param arrayBytes
	 * @author Feng
	 * @throws InterruptedException 
	 */
	public void addClientRequestToQueue(IoSession ioSession, byte[] byteArray) throws InterruptedException {
		requestQueue.put(new NetworkMessage(ioSession, byteArray));
	}
	
	/**
	 * 往“已连接用户信息表”中添加一个新用户
	 * @param key
	 * @param clientUser
	 * @author Feng
	 */
	public void addClientUserToTable(String key, ClientUser clientUser) {
		clientUserTable.put(key, clientUser);
	}
	
	/**
	 * 从“已连接用户信息表”中获取用户
	 * @param key
	 * @return ClientUser
	 * @author Feng
	 */
	public ClientUser getClientUserFromTable(String key) {
		return clientUserTable.get(key);
	}
	
	/**
	 * 添加一个等待客户端回复的监听（服务器向客户端发送消息后，要求客户端回复）
	 * @param ioSession
	 * @param key
	 * @param messageHasSent
	 * @author Feng
	 */
	public void addClientResponseListener(IoSession ioSession, byte[] key, byte[] messageHasSent) {
		WaitClientResponse waitClientResponse = new WaitClientResponse(ioSession, messageHasSent);
		waitClientResponse.time = new Date().getTime();
		// 加入到“等待回复表”中，由CheckWaitClientResponseThread 线程进行轮询
		waitClientRepTable.put(key, waitClientResponse);
	}

	/**
	 * 删除一个等待客户端回复的监听（服务器向客户端发送消息后，要求客户端回复）
	 * @param ioSession
	 * @param key
	 * @param messageHasSent
	 * @author Feng
	 */
	public void removeClientResponseListener(byte[] key) {
		waitClientRepTable.remove(key);
	}

	/**
	 * 查找一个等待客户端回复的监听（服务器向客户端发送消息后，要求客户端回复）
	 * @param ioSession
	 * @param key
	 * @param messageHasSent
	 * @author Feng
	 */
	public WaitClientResponse getClientResponseListener(byte[] key) {
		return waitClientRepTable.get(key);
	}
	
	/**
	 * 用于处理用户请求的线程
	 * 
	 * @author Feng
	 * 
	 */
	private class DealClientRequest implements Runnable {
		@Override
		public void run() {
			NetworkMessage networkMessage = null;
			// 循环获取新的请求，阻塞式
			while (true) {
				try {
					networkMessage = requestQueue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Debug.log("ServerModel", "ServerModel从请求队列中获取到一条Client发来的请求，开始交给请求分配器ClientRequest_Dispatcher处理！");
				if (networkMessage == null)
					continue;
				ClientRequest_Dispatcher.instance.dispatcher(networkMessage);

			}
		}
	}

	/**
	 * 用于定时发送心跳包
	 * 
	 * @author Feng
	 * 
	 */
	private class KeepAlivePacketSenser implements Runnable {
		@Override
		public void run() {
//			byte[] packetBytes = KeepAliveMsg.KeepAliveSyncPacket.newBuilder().build().toByteArray();
			KeepAliveMsg.KeepAliveSyncPacket.Builder packet = KeepAliveMsg.KeepAliveSyncPacket.newBuilder();
//			packet.setA(123);
//			packet.setB(true);
			packet.setC("Fuck");
			byte[] packetBytes = packet.build().toByteArray();
			try {
				// 创建心跳包
				byte[] messageBytes;

				IoBuffer responseIoBuffer;

				while (true) {
					Thread.sleep(KEEP_ALIVE_PACKET_TIME);

					messageBytes = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.KEEP_ALIVE_SYNC.getNumber(), packetBytes);
					responseIoBuffer = IoBuffer.allocate(messageBytes.length);
					responseIoBuffer.put(messageBytes);
					responseIoBuffer.flip();

					ClientUser user;
					String key;
					Debug.log("ServerModel", "开始新的一轮心跳包发送！共有 " + clientUserTable.size() + " 名用户!");
					for (Iterator it = clientUserTable.keySet().iterator(); it.hasNext();) {
						if (it == null)
							continue;
						Debug.log("ServerModel", "进入发心跳包循环!");
						key = (String) it.next();
						user = clientUserTable.get(key);

						// 将上次没有回复的干掉，从用户表中删掉
						if (user.onLine == false) {
							Debug.log("ServerModel", "Client 用户“" + user.ioSession.getRemoteAddress() + "”已掉线，即将删除！");
//							user.ioSession.close(true);

//							user.ioSession.close(true);
							clientUserTable.remove(key);
							continue;
						}

						// 发送心跳包之前先将online设为False表示不在线，若是Client回复，则重新设为True
						// ，表示在线
						Debug.log("ServerModel", "向Client " + user.ioSession.getRemoteAddress() + " 发送心跳包");
						user.onLine = false;
						user.ioSession.write(responseIoBuffer);
					}
				}
			} catch (IOException e) {
				System.err.println("发行心跳包线程异常!");
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.err.println("发行心跳包线程异常! -----睡眠模块");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 轮询"等待client回复"列表（waitClientRepTable），检查是否有超时的条目 超时的进行重发
	 * 
	 * @author Feng
	 * 
	 */
	private class CheckWaitClientResponseThread implements Runnable {
		@Override
		public void run() {
			long currentTime;
			WaitClientResponse waitObj;
			String key;
			while (true) {
				currentTime = new java.util.Date().getTime();
				// 每隔CHECK_WAIT_CLIENT_RESPONSE_DELTA_TIME时间轮询一次
				try {
					Thread.sleep(CHECK_WAIT_CLIENT_RESPONSE_DELTA_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 对每个用户进行检查
				for (Iterator iterator = waitClientRepTable.keySet().iterator(); iterator.hasNext();) {
					key = (String) iterator.next();
					waitObj = waitClientRepTable.get(key);
					if ((currentTime - waitObj.time) > WAIT_CLIENT_RESPONSE_TIMEOUT) {
						// 超时，重发
						Debug.log("ServerModel", "等待客户端" + waitObj.ioSession.getRemoteAddress() + " 回复超时！");
						System.out.println("ServerModel : 等待客户端" + waitObj.ioSession.getRemoteAddress() + " 回复超时！");
						if (!clientUserTable.get(waitObj.ioSession.getRemoteAddress()).onLine) {
							// 不在线,删了
							Debug.log("ServerModel", "客户端" + waitObj.ioSession.getRemoteAddress() + " 已断线，将从表中移除！");
							waitClientRepTable.remove(key);
							continue;
						}
						// 重发，重置等待时间
						Debug.log("ServerModel", "客户端" + waitObj.ioSession.getRemoteAddress() + " 在线，消息将重发！");
						ServerNetwork.instance.sendMessageToClient(waitObj.ioSession, waitObj.messageHasSent);
						waitObj.time = currentTime;
					}
				}
			}
		}
	}
}
