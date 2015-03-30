package server;

import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

import model.Chatting;

/**
 * 网络逻辑层(微信消息模块）
 * @author Feng
 */
public class ServerModel_Message {
	public static ServerModel_Message instance = new ServerModel_Message();

	private Hashtable<String, LinkedBlockingQueue<Chatting>> chattingHashtable;

	private ServerModel_Message() {
		chattingHashtable = new Hashtable<String, LinkedBlockingQueue<Chatting>>();
	}

	/**
	 * 往消息队列中添加一条未接收的消息
	 * @param chatting
	 * @author Feng
	 */
	public void addChatting(Chatting chatting) {
		LinkedBlockingQueue<Chatting> chattingQueue;

		if (!chattingHashtable.containsKey(chatting.getReceiverUserId())) {
			chattingQueue = new LinkedBlockingQueue<Chatting>();
			chattingHashtable.put(chatting.getReceiverUserId(), chattingQueue);
		} else
			chattingQueue = chattingHashtable.get(chatting.getReceiverUserId());

		chattingQueue.add(chatting);
	}
}
