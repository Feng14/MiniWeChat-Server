package server;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import model.Chatting;
import model.Group;
import model.HibernateDataOperation;
import model.HibernateSessionFactory;
import model.ResultCode;
import model.User;

import com.google.protobuf.InvalidProtocolBufferException;
import protocol.ProtoHead;
import protocol.Data.ChatData.ChatItem;
import protocol.Data.ChatData.ChatItem.ChatType;
import protocol.Msg.CreateGroupChatMsg;
import protocol.Msg.CreateGroupChatMsg.CreateGroupChatReq;
import protocol.Msg.CreateGroupChatMsg.CreateGroupChatRsp;
import protocol.Msg.ReceiveChatMsg.ReceiveChatSync;
import protocol.Msg.SendChatMsg.SendChatRsp;
import protocol.Msg.SendChatMsg;
import exception.NoIpException;
import tools.Debug;

/**
 * 主服务器下的子服务器，负责处理发消息相关事件
 * 
 * @author Feng
 */
public class Server_Chatting {
	private Logger logger = Logger.getLogger(this.getClass());
	private ServerModel serverModel;
	private ServerModel_Chatting serverModel_Chatting;
	private ServerNetwork serverNetwork;

	public ServerModel getServerModel() {
		return serverModel;
	}

	public void setServerModel(ServerModel serverModel) {
		this.serverModel = serverModel;
	}

	public ServerModel_Chatting getServerModel_Chatting() {
		return serverModel_Chatting;
	}

	public void setServerModel_Chatting(ServerModel_Chatting serverModel_Chatting) {
		this.serverModel_Chatting = serverModel_Chatting;
	}

	public ServerNetwork getServerNetwork() {
		return serverNetwork;
	}

	public void setServerNetwork(ServerNetwork serverNetwork) {
		this.serverNetwork = serverNetwork;
	}

	/**
	 * 对 用户发送微信消息 的事件进行处理 对方在线就立刻发送，不在则存如内存
	 * 
	 * @param networkPacket
	 * @throws NoIpException
	 * @author Feng
	 */
	public void clientSendChatting(NetworkPacket networkPacket) throws NoIpException {
		logger.debug("Server_Chatting : clientSendChatting : User sendChatting Event :Deal with user's "
				+ ServerModel.getIoSessionKey(networkPacket.ioSession) + "  request");
		// Debug.log(new String[] { "Server_Chatting", "clientSendChatting" },
		// " User sendChatting Event :Deal with user's "
		// + ServerModel.getIoSessionKey(networkPacket.ioSession) +
		// "  send chatting event");

		// 构造回复对象
		SendChatMsg.SendChatRsp.Builder sendChattingResponse = SendChatMsg.SendChatRsp.newBuilder();
		sendChattingResponse.setResultCode(SendChatRsp.ResultCode.FAIL);

		try {
			SendChatMsg.SendChatReq sendChattingObject = SendChatMsg.SendChatReq.parseFrom(networkPacket.getMessageObjectBytes());

			ChatItem chatItem = sendChattingObject.getChatData();

			// 检查接受者是群还是个人
			if (chatItem.getTargetType() == ChatItem.TargetType.INDIVIDUAL)
				clientSendChatting_Individual(networkPacket, chatItem);
			else if (chatItem.getTargetType() == ChatItem.TargetType.GROUP)
				clientSendChatting_Group(networkPacket, chatItem);

			sendChattingResponse.setResultCode(SendChatMsg.SendChatRsp.ResultCode.SUCCESS);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			logger.error(e.toString());
		} catch (Exception e) {
			logger.error(e.toString());
			e.printStackTrace();
		}
		// 回复客户端说发送成功(保存在服务器成功)
		serverNetwork.sendToClient(new WaitClientResponse(networkPacket.ioSession, new PacketFromServer(networkPacket
				.getMessageID(), ProtoHead.ENetworkMessage.SEND_CHAT_RSP_VALUE, sendChattingResponse.build().toByteArray())));
	}

	/**
	 * 对 用户发送(个人)微信消息 的事件进行处理 对方在线就立刻发送，不在则存如内存
	 * 
	 * @param networkPacket
	 * @param chatItem
	 * @param chatting
	 * @throws NoIpException
	 * @author Feng
	 */
	private void clientSendChatting_Individual(NetworkPacket networkPacket, ChatItem chatItem)
			throws NoIpException {
		Chatting chatting = new Chatting(chatItem.getSendUserId(), chatItem.getReceiveUserId(), chatItem.getChatType(),
				chatItem.getChatBody(), Calendar.getInstance().getTimeInMillis());
		
		// 若是接收者在线，则发送，否则加入队列
		ClientUser clientUser = serverModel.getClientUserByUserId(chatting.getReceiverUserId());
		if (clientUser != null) {
			logger.debug("Server_Chatting : clientSendChatting : Receiver online,send to receier("
					+ ServerModel.getIoSessionKey(clientUser.ioSession) + ") now!");
			// Debug.log(new String[] { "Server_Chatting", "clientSendChatting"
			// },
			// "Receiver online,send to receier(" +
			// ServerModel.getIoSessionKey(clientUser.ioSession) + ") now!");

			// 发送给接收者
			serverModel_Chatting.sendChatting(chatting);
		} else {
			// 不在线，保存
			logger.debug("Server_Chatting : clientSendChatting : Receiver offline,save to memory!");
			// Debug.log(new String[] { "Server_Chatting", "clientSendChatting"
			// }, "Receiver offline,save to memory!");
			serverModel_Chatting.addChatting(chatting);
		}
	}

	/**
	 * 对 用户发送(群)微信消息 的事件进行处理, 立刻发送
	 * 
	 * @param networkPacket
	 * @param chatItem
	 * @param chatting
	 * @author Feng
	 * @throws Exception
	 */
	private void clientSendChatting_Group(NetworkPacket networkPacket, ChatItem chatItem) throws Exception {
		ResultCode resultCode = ResultCode.FAIL;
		Session session = HibernateSessionFactory.getSession();
		
		List<Group> groupList = HibernateDataOperation.query(Group.GROUP_ID, Integer.parseInt(chatItem.getReceiveUserId()),
				Group.class, resultCode, session);
		Group group = groupList.get(0);
		List<User> receiverList = group.getMemberList();
//		List<User> receiverList = ((List<User>) HibernateDataOperation.query(Group.GROUP_ID, Integer.parseInt(chatItem.getReceiveUserId()),
//				Group.class, resultCode, session).get(0));

		if (resultCode.getCode() == ResultCode.FAIL || receiverList == null) {
			logger.error("Server_Chatting : clientSendChatting_Group : Get Group Error!");
			throw new Exception("Server_Chatting : clientSendChatting_Group : Get Group Error!");
		}

		Chatting chatting;
		// 给每个组员发一份
		for (User user : receiverList) {
			chatting = new Chatting(chatItem.getSendUserId(), user.getUserId(), chatItem.getChatType(), chatItem.getChatBody(),
					Calendar.getInstance().getTimeInMillis(), true, Integer.parseInt(chatItem.getReceiveUserId()));
			
			serverModel_Chatting.sendChatting(chatting);
		}

	}

	/**
	 * 用户创建群聊
	 * 
	 * @param networkPacket
	 * @throws NoIpException
	 * @author Feng
	 */
	public void createGroupChatting(NetworkPacket networkPacket) throws NoIpException {
		logger.debug("Server_Chatting : createGroupChatting : User CreateGroupChatting Event :Deal with user's "
				+ ServerModel.getIoSessionKey(networkPacket.ioSession) + "  request");

		// Debug.log(new String[] { "Server_Chatting", "clientSendChatting" },
		// " User sendChatting Event :Deal with user's "
		// + ServerModel.getIoSessionKey(networkPacket.ioSession) +
		// "  send chatting event");

		// 构造回复对象
		CreateGroupChatMsg.CreateGroupChatRsp.Builder createGroupChattingResponse = CreateGroupChatRsp.newBuilder();
		createGroupChattingResponse.setResultCode(CreateGroupChatRsp.ResultCode.FAIL);

		try {
			CreateGroupChatReq createGroupChattingObj = CreateGroupChatReq.parseFrom(networkPacket.getMessageObjectBytes());

			List<String> userIdList = createGroupChattingObj.getUserIdList();
			if (userIdList.size() > 0) {
				// 创建群名
				String groupName = "";
				for (int i = 0; i < userIdList.size() && i < 3; i++)
					groupName += userIdList.get(i).toString() + ",";
				groupName = groupName.substring(0, (groupName.length() > 10 ? 10 : groupName.length())) + "...";

				Group group = new Group(groupName);

				// 加入用户
				String hql = "from " + User.class.getSimpleName() + " where " + User.TABLE_USER_ID + " in (";
				boolean containSelf = false;
				ClientUser selfUser1 = serverModel.getClientUserFromTable(networkPacket.ioSession);
				if (selfUser1 == null) {
					logger.error("Server_Chatting : createGroupChatting : creater is offLine!");
				} else {
					for (String userID : userIdList) {
						hql += "'" + userID + "',";
						// 看看自己有没有被加进去
						if (userID.equals(selfUser1.userId))
							containSelf = true;
					}
					// 若是列表中没有自己，则加进去
					if (!containSelf)
						hql += "'" + selfUser1.userId + "'";
					else
						hql = hql.substring(0, hql.length() - 1);

					hql += ")";

					Session session = HibernateSessionFactory.getSession();
					// String hql = "from " + User.class.getName() + " where " +
					// User.TABLE_USER_ID + " in('a','b')";
					// List<User> userList2 = session.createQuery(hql).list();
					List<User> userList = session.createQuery(hql).list();
					group.setMemberList(userList);

					// 设置创建者
					User selfUser2 = null;
					for (User user : userList)
						if (user.getUserId().equals(selfUser1.userId)) {
							group.setCreaterId(selfUser1.userId);
							break;
						}

					// 保存
					HibernateSessionFactory.commitSession(session);
					session = HibernateSessionFactory.getSession();
					
					ResultCode resultCode = ResultCode.NULL;
					HibernateDataOperation.add(group, resultCode, session);
					if (resultCode.getCode() != ResultCode.SUCCESS)
						throw new Exception("Server_Chatting : createGroupChatting : save newGroup Objcet Error!");
					HibernateSessionFactory.commitSession(session);

					// 设置回复的群号
					createGroupChattingResponse.setGroupChatId(group.getGroupId());

					// 如果成功，设置标志位
					logger.debug("Server_Chatting : createGroupChatting : create group chatting Successful, response to client!");
					if (resultCode.getCode() == ResultCode.SUCCESS)
						createGroupChattingResponse.setResultCode(CreateGroupChatRsp.ResultCode.SUCCESS);

//					session.close();
				}
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			logger.error(e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
		}
		// 回复客户端说发送成功(保存在服务器成功)
		serverNetwork.sendToClient(new WaitClientResponse(networkPacket.ioSession, new PacketFromServer(networkPacket
				.getMessageID(), ProtoHead.ENetworkMessage.CREATE_GROUP_CHAT_VALUE, createGroupChattingResponse.build()
				.toByteArray())));
	}

	/**
	 * 发消息自动回复（调试测试用）
	 * 
	 * @param networkPacket
	 * @param chatting
	 * @author Feng
	 * @throws IOException
	 */
	private void sendChattingAutoResponse(NetworkPacket networkPacket, Chatting chatting) throws IOException {
		// 回复发送者：发送成功
		SendChatMsg.SendChatRsp.Builder sendChattingResponse = SendChatMsg.SendChatRsp.newBuilder();
		sendChattingResponse.setResultCode(SendChatMsg.SendChatRsp.ResultCode.SUCCESS);
		serverNetwork.sendToClient(networkPacket.ioSession, new PacketFromServer(networkPacket.getMessageID(),
				ProtoHead.ENetworkMessage.SEND_CHAT_RSP_VALUE, sendChattingResponse.build().toByteArray()));

		// 自动回复
		ReceiveChatSync.Builder receiverChatObj = ReceiveChatSync.newBuilder();
		ChatItem.Builder chatItem = chatting.createChatItem();
		chatItem.setSendUserId(chatting.getReceiverUserId());
		chatItem.setReceiveUserId(chatting.getSenderUserId());
		receiverChatObj.addChatData(chatItem);

		// 发送
		serverNetwork.sendToClient(networkPacket.ioSession, new PacketFromServer(
				ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE, receiverChatObj.build().toByteArray()));

		// byte[] messageWillSend =
		// networkPacket.packMessage(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE,
		// receiverChatObj
		// .build().toByteArray());
		// 添加回复监听
		// serverModel_Chatting.addListenReceiveChatting(networkPacket.ioSession,
		// chatting, messageWillSend);
	}

	// private void sendChatSuperCommand(networkPacket networkPacket, Chatting
	// chatting) throws IOException {
	// // 回复发送者：发送成功
	// SendChatMsg.SendChatRsp.Builder sendChattingResponse =
	// SendChatMsg.SendChatRsp.newBuilder();
	// sendChattingResponse.setResultCode(SendChatMsg.SendChatRsp.ResultCode.SUCCESS);
	// serverNetwork.sendMessageToClient(networkPacket.ioSession,
	// networkPacket.packMessage(
	// ProtoHead.ENetworkMessage.SEND_CHAT_RSP_VALUE,
	// networkPacket.getMessageID(), sendChattingResponse.build()
	// .toByteArray()));
	// }

	/**
	 * 客户端已接收到服务其发送的“未接收消息”， 删除对客户端回复的等待
	 * 
	 * @param networkPacket
	 */
	// public void clientReceiveChatting(networkPacket networkPacket) {
	// byte[] key = networkPacket.getMessageID(networkPacket.arrayBytes);
	// serverModel.removeClientResponseListener(key);
	// }
}
