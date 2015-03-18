package server;

import java.util.HashSet;
import java.util.concurrent.BlockingQueue;

/**
 * 网络逻辑层
 * @author Feng
 *
 */
public class ServerModel {
	public static ServerModel instance;
	// 请求队列
	public BlockingQueue<NetworkMessage> requestQueue = new NetworkMessageQueue();
	// 用户列表
	public HashSet<ClientUser> clientUserSet = new HashSet<ClientUser>();
	
	private ServerModel() {
		
	}
	
	public void init() {
		
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
				System.out.println("ServerModel从请求队列中获取到一条Client发来的请求，开始交给请求分配器处理！");
				ClientRequest_Dispatcher.instance.dispatcher(networkMessage);
				
			}
		}
	}
	
	// 用于定时发送心跳包
	private class KeepAlivePacketSenser implements Runnable {

		@Override
		public void run() {
			for (ClientUser user : clientUserSet) {
				
			}
			// TODO Auto-generated method stub
			
		}
		
	}
}
