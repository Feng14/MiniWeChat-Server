package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import model.HibernateSessionFactory;
import model.User;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.sun.org.apache.bcel.internal.generic.NEW;

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
	// 已连接用户信息表(Key 为IoSession.getRemoteAddress().toString)
	private Hashtable<String, ClientUser> clientUserTable = new Hashtable<String, ClientUser>();
	// 监听客户端回复的表
	private Hashtable<byte[], WaitClientResponse> waitClientRepTable = new Hashtable<byte[], WaitClientResponse>();

	private ServerModel() {
//		System.out.println("Fuycj");
//		Session session = HibernateSessionFactory.getSession();
//		Criteria criteria = session.createCriteria(User.class);
//		criteria.add(Restrictions.eq("userId", "a"));
//
//		System.out.println(1);
//		User user = (User) criteria.list().get(0);
//		System.out.println(2);
//		System.out.println(user.getFriends().get(0).getUserId());
//		System.out.println(3);
//		System.out.println(user.getFriends().get(1).getUserName());
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
		new Thread(new DealClientRequest()).start();
		new Thread(new KeepAlivePacketSenser()).start();
		new Thread(new CheckWaitClientResponseThread()).start();
	}

	/**
	 * 往客户端请求列表中加入一条请求
	 * 
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
	 * @param ioSession
	 * @return
	 * @throws NoIpException 
	 */
	public static String getIoSessionKey(IoSession ioSession) throws NoIpException {
//		System.err.println("1.2  " + (ioSession.getRemoteAddress() == null));
		if (ioSession.getRemoteAddress() == null)
			throw new NoIpException();
		return ((InetSocketAddress)ioSession.getRemoteAddress()).getAddress().toString() + ":" + ((InetSocketAddress)ioSession.getRemoteAddress()).getPort();
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
	 * 
	 * @param ioSession
	 * @param key
	 * @param messageHasSent
	 * @author Feng
	 */
	public void removeClientResponseListener(byte[] key) {
		synchronized (waitClientRepTable) {
			waitClientRepTable.remove(key);
		}
	}

	/**
	 * 查找一个等待客户端回复的监听（服务器向客户端发送消息后，要求客户端回复）
	 * 
	 * @param ioSession
	 * @param key
	 * @param messageHasSent
	 * @author Feng
	 */
	public WaitClientResponse getClientResponseListener(byte[] key) {
		synchronized (waitClientRepTable) {
			return waitClientRepTable.get(key);
		}
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
			synchronized (waitClientRepTable) {
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
			KeepAliveMsg.KeepAliveSyncPacket.Builder packet = KeepAliveMsg.KeepAliveSyncPacket.newBuilder();
			byte[] packetBytes = packet.build().toByteArray();
			// 创建心跳包
			byte[] messageBytes;

			IoBuffer responseIoBuffer;
			ArrayList<String> keyIterators;

			synchronized (clientUserTable) {
				while (true) {
					try {
						Thread.sleep(KEEP_ALIVE_PACKET_TIME);
						
						ClientUser user;
						Iterator iterator = clientUserTable.keySet().iterator();
						String key;
						
						Debug.log("ServerModel", "开始新的一轮心跳包发送！共有 " + clientUserTable.size() + " 名用户!");
						while (iterator.hasNext()) {
//					for (String key : keyIterators) {
							Debug.log("ServerModel", "进入发心跳包循环!");
							
							key = iterator.next().toString();
							
							if (!clientUserTable.containsKey(key))
								continue;
							user = clientUserTable.get(key);
							
							// 若已死，删除    ;   将上次没有回复的干掉，从用户表中删掉
							if (user.die || user.onLine == false) {
								Debug.log("ServerModel", "Client 用户“" + user.ioSession.getRemoteAddress() + "”已掉线，即将删除！");
								// user.ioSession.close(true);
								iterator.remove();
								continue;
							}
							
							messageBytes = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.KEEP_ALIVE_SYNC.getNumber(), packetBytes);
							responseIoBuffer = IoBuffer.allocate(messageBytes.length);
							responseIoBuffer.put(messageBytes);
							responseIoBuffer.flip();
							
							// 发送心跳包之前先将online设为False表示不在线，若是Client回复，则重新设为True
							// ，表示在线
							Debug.log("ServerModel", "向Client " + user.ioSession.getRemoteAddress() + " 发送心跳包");
							user.onLine = false;
							user.ioSession.write(responseIoBuffer);
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
			synchronized (waitClientRepTable) {
				while (true) {
					currentTime = new java.util.Date().getTime();
					// 每隔CHECK_WAIT_CLIENT_RESPONSE_DELTA_TIME时间轮询一次
					try {
						Thread.sleep(CHECK_WAIT_CLIENT_RESPONSE_DELTA_TIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// 对每个用户进行检查
					Iterator iterator = waitClientRepTable.keySet().iterator();
					while (iterator.hasNext()) {
						key = iterator.next().toString();
						waitObj = waitClientRepTable.get(key);
						if (waitObj == null)
							continue;
						
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
}
