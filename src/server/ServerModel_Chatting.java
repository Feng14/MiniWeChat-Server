package server;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.mina.core.session.IoSession;
import exception.NoIpException;
import observer.ObserverMessage;
import observer.ObserverMessage_Login;
import protocol.Msg.ReceiveChatMsg.ReceiveChatSync;

import model.Chatting;

/**
 * 网络逻辑层(微信消息模块）
 * 
 * @author Feng
 */
public class ServerModel_Chatting {
	public static ServerModel_Chatting instance = new ServerModel_Chatting();

	private Hashtable<String, LinkedBlockingQueue<Chatting>> chattingHashtable;

	private ServerModel_Chatting() {
		chattingHashtable = new Hashtable<String, LinkedBlockingQueue<Chatting>>();

		// 监听用户登陆事件
		ServerModel.instance.addObserver(new Observer() {
			/**
			 * 检查是否有未接收的消息
			 */
			@Override
			public void update(Observable o, Object arg) {
				ObserverMessage om = (ObserverMessage) arg;
				if (om.type == ObserverMessage.Type.Login) {
					ObserverMessage_Login oml = (ObserverMessage_Login) om;
					Debug.log(new String[] { "ServerModel_Chatting", "ServerModel_Chatting" }, "监听到用户 " + oml.userId + " 的登陆事件！");

					ArrayList<Chatting> chattingList = getChattingNotReceive(oml.userId);
					if (chattingList != null && chattingList.size() > 0) {
						Debug.log(new String[] { "ServerModel_Chatting", "ServerModel_Chatting" }, "用户 " + oml.userId + " 有"
								+ chattingList.size() + "条未接收消息，开始发送！");

						ReceiveChatSync.Builder receiveChatting = ReceiveChatSync.newBuilder();

						// 加入所有未接收消息
						for (Chatting chatting : chattingList)
							receiveChatting.addChatData(chatting.createChatItem());

						byte[] messageWillSend = receiveChatting.build().toByteArray();
						// 添加监听
						addListenReceiveChatting(oml.ioSession, chattingList, messageWillSend);

						try {
							ServerNetwork.instance.sendMessageToClient(oml.ioSession,
									NetworkMessage.packMessage(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE, messageWillSend));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}

	/**
	 * 发送“未接收消息”和添加“等待客户端回复：已收到”监听
	 * 
	 * @author Feng
	 * @param ioSession
	 * @param chatting
	 */
	public void addListenReceiveChatting(IoSession ioSession, Chatting chatting, byte[] messageWillSend) {
		ArrayList<Chatting> chattingList = new ArrayList<Chatting>(1);
		chattingList.add(chatting);
		addListenReceiveChatting(ioSession, chattingList, messageWillSend);
	}

	public void addListenReceiveChatting(final IoSession ioSession, final ArrayList<Chatting> chattingList, byte[] messageWillSend) {
		ServerModel.instance.addClientResponseListener(ioSession, NetworkMessage.getMessageID(messageWillSend), messageWillSend,
				new WaitClientResponseCallBack() {

					@Override
					public void beforeDelete() {
						// 保存回未发送队列
						try {
							String key = ServerModel.getIoSessionKey(ioSession);
							LinkedBlockingQueue<Chatting> chattingQueue;
							if (!chattingHashtable.containsKey(key)) {
								chattingQueue = new LinkedBlockingQueue<Chatting>();
								chattingHashtable.put(key, chattingQueue);
							} else
								chattingQueue = chattingHashtable.get(key);

							for (Chatting chatting : chattingList)
								chattingQueue.add(chatting);
						} catch (NoIpException e) {
							e.printStackTrace();
						}
					}
				});
	}

	/**
	 * 往消息队列中添加一条未接收的消息
	 * 
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

	public ArrayList<Chatting> getChattingNotReceive(String receiveUserId) {
		if (chattingHashtable.containsKey(receiveUserId)) {
			LinkedBlockingQueue<Chatting> chattingQueue = chattingHashtable.get(receiveUserId);
			ArrayList<Chatting> chattingList = new ArrayList<Chatting>();
			while (!chattingQueue.isEmpty())
				chattingList.add(chattingQueue.poll());

			return chattingList;
		}
		return new ArrayList<Chatting>();
	}
}
