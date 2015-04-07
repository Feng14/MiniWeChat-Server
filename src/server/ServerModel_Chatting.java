package server;

import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.session.IoSession;
import org.hibernate.Session;

import observer.ObserverMessage;
import observer.ObserverMessage_Login;
import protocol.ProtoHead;
import protocol.Data.ChatData.ChatItem.ChatType;
import protocol.Msg.ReceiveChatMsg.ReceiveChatSync;
import tools.Debug;

import model.Chatting;
import model.HibernateSessionFactory;

/**
 * 网络逻辑层(微信消息模块）
 * 
 * @author Feng
 */
public class ServerModel_Chatting {
	public static final int SAVE_DATA_HOUR = 1;
	public static final int INTERVAL_HOUR = 24 * 60 * 60;
	public static final int DELETE_INTERVAL = 24 * 3 * 60 * 60;

	public static ServerModel_Chatting instance = new ServerModel_Chatting();

	public Hashtable<String, LinkedBlockingQueue<Chatting>> chattingHashtable;

	private ServerModel_Chatting() {
		chattingHashtable = new Hashtable<String, LinkedBlockingQueue<Chatting>>();
		System.out.println(chattingHashtable.containsKey("b"));

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
					Debug.log(new String[] { "ServerModel_Chatting", "ServerModel_Chatting" }, "Catch User( " + oml.userId
							+ ") 'LoginEvent'！");

					ArrayList<Chatting> chattingList = getChattingNotReceive(oml.userId);
					if (chattingList != null && chattingList.size() > 0) {
						Debug.log(new String[] { "ServerModel_Chatting", "ServerModel_Chatting" }, "User(" + oml.userId + ") has"
								+ chattingList.size() + " 'Chattings' doesn't be received，Start transmit！");

						ReceiveChatSync.Builder receiveChatting = ReceiveChatSync.newBuilder();

						// 加入所有未接收消息
						for (Chatting chatting : chattingList)
							receiveChatting.addChatData(chatting.createChatItem());

						byte[] messageWillSend = receiveChatting.build().toByteArray();
						// 添加监听
						addListenReceiveChatting(oml.ioSession, chattingList, messageWillSend);

						try {
							ServerNetwork.instance.sendMessageToClient(oml.ioSession, NetworkMessage.packMessage(
									ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE, messageWillSend));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});

		// 添加每日聊天记录存入数据库
		// Date firstStartDate = new Date();
		// firstStartDate.setDate(firstStartDate.getDate() + 1);
		// firstStartDate.setHours(SAVE_DATA_HOUR);
		// Timer timer = new Timer();
		// timer.schedule(new SaveDataThread(), firstStartDate, INTERVAL_HOUR);
		// test();
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
						Debug.log(Debug.LogType.ERROR, new String[] { "ServerModel_Chatting", "addListenReceiveChatting" },
								" 'Chatting' send fail，save to Memory！");
						if (chattingList.size() == 0)
							return;
						String key = chattingList.get(0).getReceiverUserId();
						LinkedBlockingQueue<Chatting> chattingQueue;
						if (!chattingHashtable.containsKey(key)) {
							chattingQueue = new LinkedBlockingQueue<Chatting>();
							chattingHashtable.put(key, chattingQueue);
						} else
							chattingQueue = chattingHashtable.get(key);

						for (Chatting chatting : chattingList)
							chattingQueue.add(chatting);
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

	/**
	 * 将内存中所有聊天记录存入数据库
	 * 
	 * @author Feng
	 */
	public class SaveDataThread extends TimerTask {
		public void run() {
			try {
				Iterator iterator = chattingHashtable.keySet().iterator();
				LinkedBlockingQueue<Chatting> queue;

				// 删除过期消息
				Calendar calendar = Calendar.getInstance();
				calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR) - 3);

				Session session = HibernateSessionFactory.getSession();
				String sql = "delete from " + Chatting.TABLE_NAME + " where time<" + calendar.getTimeInMillis() + ";";
				session.createQuery(sql);

				session = HibernateSessionFactory.getSession();
				while (iterator.hasNext()) {
					queue = (LinkedBlockingQueue<Chatting>) iterator.next();

					// 读取哈希表，存入硬盘
					for (Chatting chatting : queue) {
						session.save(chatting);
					}
				}
				HibernateSessionFactory.commitSession(session);

				// 清空内存
				chattingHashtable.clear();
			} catch (Exception e) {
				Debug.log(Debug.LogType.FAULT, new String[] { "ServerModel_Chatting", "SaveDataThread" },
						"Save Chatting which were to long ago Fail!");
			}
		}
	}

	public void test2() {
		Iterator iterator = chattingHashtable.keySet().iterator();
		LinkedBlockingQueue<Chatting> queue;

		// 删除过期消息
		Date date = new Date();
		date.setDate(date.getDate() - 3);
		// date.setDate(date.getDate() + 3);
		Session session = HibernateSessionFactory.getSession();
		String tableName = Chatting.TABLE_NAME;
		tableName = tableName.substring(0, 1).toUpperCase() + tableName.substring(2, tableName.length());
		String sql = "delete from " + Chatting.class.getName() + " where time<" + date.getTime();
		session.createQuery(sql);

		HibernateSessionFactory.commitSession(session);
		session = HibernateSessionFactory.getSession();

		session = HibernateSessionFactory.getSession();
		Chatting a = new Chatting("a3", "a4", ChatType.TEXT, "abcde", 20140526, false, 2);
		a.setId(1);
		session.save(a);

		while (iterator.hasNext()) {
			queue = chattingHashtable.get(iterator.next().toString());

			// 读取哈希表，存入硬盘
			for (Chatting chatting : queue) {
				Chatting c = new Chatting(chatting.getSenderUserId(), chatting.getReceiverUserId(), chatting.getChattingType(),
						chatting.getMessage(), Calendar.getInstance().getTimeInMillis());
				session.save(c);
				// session.save(chatting);
			}
		}
		HibernateSessionFactory.commitSession(session);

		// 清空内存
		chattingHashtable.clear();
	}

	/**
	 * 测试用
	 * 
	 * @param args
	 * @author Feng
	 */
	// private void test() {
	// addChatting(new Chatting("a", "b", ChatType.TEXT, "Fuck", 0));
	// addChatting(new Chatting("c", "d", ChatType.TEXT, "Fuck", 0));
	// System.out.println("size: " + chattingHashtable.size());
	//
	// // Timer timer = new Timer();
	// // timer.schedule(new SaveDataThread(), 0);
	// // test2();
	// // System.out.println("size: " + chattingHashtable.size());
	// }
}
