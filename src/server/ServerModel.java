package server;

import java.io.IOException;
import java.sql.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.buffer.IoBuffer;

import com.sun.org.apache.bcel.internal.generic.NEW;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;
import tools.DataTypeTranslater;

/**
 * 网络逻辑层
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
	// 请求队列
	public LinkedBlockingQueue<NetworkMessage> requestQueue = new LinkedBlockingQueue<NetworkMessage>();
	// 用户列表
	public Hashtable<String, ClientUser> clientUserTable = new Hashtable<String, ClientUser>();
	//监听客户端回复的表
	public Hashtable<Byte[], WaitClientResponse> waitClientRepTable = new Hashtable<Byte[], WaitClientResponse>();
	
	
	private ServerModel() {
		
	}
	/**
	 * 创建一个随机的MessageId
	 * @return
	 */
	public static byte[] createMessageId(){
		return DataTypeTranslater.floatToBytes((float)Math.random());
	}
	
	/**
	 *  初始化
	 */
	public void init() {
		// 开始新线程
		new Thread(new DealClientRequest()).start();
		new Thread(new KeepAlivePacketSenser()).start();
	}
	
	// 用于处理用户请求的线程
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
				System.out.println("ServerModel: ServerModel从请求队列中获取到一条Client发来的请求，开始交给请求分配器处理！");
				if (networkMessage == null)
					continue;
				ClientRequest_Dispatcher.instance.dispatcher(networkMessage);
				
			}
		}
	}
	
	/**
	 * 用于定时发送心跳包
	 * @author Administrator
	 *
	 */
	private class KeepAlivePacketSenser implements Runnable {
		@Override
		public void run() {
			byte[] packetBytes = KeepAliveMsg.KeepAliveSyncPacket.newBuilder().build().toByteArray();
			try {
				// 创建心跳包
				byte[] messageBytes;
				
				IoBuffer responseIoBuffer;
				
				while (true) {
					Thread.sleep(KEEP_ALIVE_PACKET_TIME);
					
					messageBytes = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.KeepAliveSync.getNumber(), packetBytes);
					responseIoBuffer = IoBuffer.allocate(messageBytes.length);
					responseIoBuffer.put(messageBytes);
					responseIoBuffer.flip();
					
					ClientUser user;
					String key;
					for (Iterator it = clientUserTable.keySet().iterator(); it.hasNext();) {
						key = (String)it.next();
						user = clientUserTable.get(key);
						// 将上次没有回复的干掉，从用户表中删掉
						if (user.onLine == false) {
							System.out.println("Client 用户“" + user.ioSession.getRemoteAddress() + "”已掉线，即将删除！");
							user.ioSession.close(true);
							
							clientUserTable.remove(key);
							continue;
						}
						
						// 发送心跳包之前先将online设为False表示不在线，若是Client回复，则重新设为True ，表示在线
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
	 * 轮询"等待client回复"列表（waitClientRepTable），检查是否有超时的条目
	 * 超时的进行重发
	 * @author Feng
	 *
	 */
	class CheckWaitClientResponseThread implements Runnable {
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
				for (Iterator iterator = waitClientRepTable.keySet().iterator(); iterator.hasNext();){
					key = (String)iterator.next();
					waitObj = waitClientRepTable.get(key);
					if ((currentTime - waitObj.time) > WAIT_CLIENT_RESPONSE_TIMEOUT) {
						// 超时，重发
						if (!clientUserTable.get(waitObj.ioSession.getRemoteAddress()).onLine) {
							// 不在线,删了
							waitClientRepTable.remove(key);
							continue;
						}
						// 重发，重置等待时间
						ServerNetwork.instance.sendMessageToClient(waitObj.ioSession, waitObj.messageHasSent);
						waitObj.time = currentTime;
					}
				}
			}
		}
		
	}
}
