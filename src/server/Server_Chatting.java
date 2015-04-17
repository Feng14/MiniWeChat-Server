package server;

import java.io.IOException;
import java.util.ArrayList;
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
import protocol.Data.ChatData.ChatItem.TargetType;
import protocol.Data.GroupData.GroupItem;
import protocol.Data.UserData.UserItem;
import protocol.Msg.ChangeGroupChatMemberMsg.ChangeGroupChatMemberRsp;
import protocol.Msg.ChangeGroupChatMemberMsg.ChangeGroupChatMemberRsq;
import protocol.Msg.ChangeGroupChatMemberMsg.ChangeGroupChatMemberSync;
import protocol.Msg.ChangeGroupChatMemberMsg.ChangeGroupChatMemberRsq.ChangeType;
import protocol.Msg.CreateGroupChatMsg;
import protocol.Msg.CreateGroupChatMsg.CreateGroupChatReq;
import protocol.Msg.CreateGroupChatMsg.CreateGroupChatRsp;
import protocol.Msg.GetGroupInfoMsg.GetGroupInfoReq;
import protocol.Msg.GetGroupInfoMsg.GetGroupInfoRsp;
import protocol.Msg.ReceiveChatMsg.ReceiveChatSync;
import protocol.Msg.SendChatMsg.SendChatRsp;
import protocol.Msg.SendChatMsg;
import exception.MyException;
import exception.NoIpException;

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
	private Server_User server_User;

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

	public Server_User getServer_User() {
		return server_User;
	}

	public void setServer_User(Server_User server_User) {
		this.server_User = server_User;
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
				clientSendChatting_Group(chatItem);

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
	private void clientSendChatting_Individual(NetworkPacket networkPacket, ChatItem chatItem) throws NoIpException {
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
	 * @param chatItem
	 * @param chatting
	 * @author Feng
	 * @throws Exception
	 */
	private void clientSendChatting_Group(ChatItem chatItem) throws Exception {
		ResultCode resultCode = ResultCode.FAIL;
		Session session = HibernateSessionFactory.getSession();

		List<Group> groupList = HibernateDataOperation.query(Group.GROUP_ID, Integer.parseInt(chatItem.getReceiveUserId()),
				Group.class, resultCode, session);
		Group group = groupList.get(0);
		List<User> receiverList = group.getMemberList();
		// List<User> receiverList = ((List<User>)
		// HibernateDataOperation.query(Group.GROUP_ID,
		// Integer.parseInt(chatItem.getReceiveUserId()),
		// Group.class, resultCode, session).get(0));

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
			if (userIdList.size() <= 0)
				throw new MyException("Server_Chatting : createGroupChatting : Member Size = 0");
			// 创建群名
			String groupName = "";
			for (int i = 0; i < userIdList.size() && i < 3; i++)
				groupName += userIdList.get(i).toString() + ",";
			groupName = groupName.substring(0, (groupName.length() > 10 ? 10 : groupName.length())) + "...";

			// 判断本用户知否在线
			ClientUser selfUser1 = serverModel.getClientUserFromTable(networkPacket.ioSession);
			if (selfUser1 == null)
				throw new MyException("Server_Chatting : createGroupChatting : creater is offLine!");

			// 加入自己
			if (!userIdList.contains(selfUser1.userId))
				userIdList.add(selfUser1.userId);

			Session session = HibernateSessionFactory.getSession();
			// 创建群对象
			Group group = new Group(groupName);
			// 设置成员
			List<User> userList = server_User.getUsers(userIdList, session);
			group.setMemberList(userList);
			// 设置创建者
			group.setCreaterId(selfUser1.userId);

			// 保存
			ResultCode resultCode = ResultCode.NULL;
			HibernateDataOperation.add(group, resultCode, session);
			HibernateSessionFactory.commitSession(session);
			if (resultCode.getCode() != ResultCode.SUCCESS)
				throw new MyException("Server_Chatting : createGroupChatting : save newGroup Objcet Error!");

			// 设置回复的群号
			createGroupChattingResponse.setGroupChatId(group.getGroupId());

			// 如果成功，设置标志位
			logger.debug("Server_Chatting : createGroupChatting : create group chatting Successful, response to client!");
			
			if (resultCode.getCode() == ResultCode.SUCCESS)
				createGroupChattingResponse.setResultCode(CreateGroupChatRsp.ResultCode.SUCCESS);
			
			// 回复客户端
			serverNetwork.sendToClient(new WaitClientResponse(networkPacket.ioSession, new PacketFromServer(networkPacket
					.getMessageID(), ProtoHead.ENetworkMessage.CREATE_GROUP_CHAT_RSP_VALUE, createGroupChattingResponse.build()
					.toByteArray())));
			// 通知每位群成员修改列表
			sendChangeGroupSync(group, userList, ChangeGroupChatMemberSync.Type.REFRESH);
			// 消息通知成员
			notifyMemberJionIn1(selfUser1.userId, userList, group.getGroupId()+"");
			
			return;
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			logger.error(e.toString());
		} catch (MyException e) {
			logger.error(e.toString());
		}
		// 回复客户端
		createGroupChattingResponse.setResultCode(CreateGroupChatRsp.ResultCode.FAIL);
		serverNetwork.sendToClient(new WaitClientResponse(networkPacket.ioSession, new PacketFromServer(networkPacket
				.getMessageID(), ProtoHead.ENetworkMessage.CREATE_GROUP_CHAT_RSP_VALUE, createGroupChattingResponse.build()
				.toByteArray())));
	}

	/**
	 * 获取一个聊天群资料
	 * 
	 * @param networkPacket
	 * @author Feng
	 * @throws NoIpException
	 */
	public void getGroupInfo(NetworkPacket networkPacket) throws NoIpException {
		logger.debug("Server_Chatting : getGroupInfo : Client GetGroupInfo Event :Deal with user's "
				+ ServerModel.getIoSessionKey(networkPacket.ioSession) + "  request");

		// 构造回复对象
		GetGroupInfoRsp.Builder responseBuilder = GetGroupInfoRsp.newBuilder();
		responseBuilder.setResultCode(GetGroupInfoRsp.ResultCode.FAIL);

		try {
			// 获取请求对象
			GetGroupInfoReq requestObj = GetGroupInfoReq.parseFrom(networkPacket.getMessageObjectBytes());

			// 获取群资料
			Session session = HibernateSessionFactory.getSession();
			ResultCode resultCode = ResultCode.NULL;
			Group group = getGroupInfo(Integer.parseInt(requestObj.getGroupId()), session);

			if (group == null) {
				responseBuilder.setResultCode(GetGroupInfoRsp.ResultCode.GROUP_NOT_EXIST);
				throw new MyException("Server_Chatting : getGroupInfo(NetworkPacket) : Can't get Group, Group = null");
			}

			GroupItem.Builder groupItem = GroupItem.newBuilder();
			UserItem.Builder userItemBuilder = UserItem.newBuilder();

			// 设置创建者
			responseBuilder.setCreaterId(group.getCreaterId());
			// groupItem.setCreater(User.createUserItemBuilder(server_User.getUser(group.getCreaterId(),
			// session)));
			// 添加成员
			for (User user : group.getMemberList())
				responseBuilder.addMemberId(user.getUserId());
			// groupItem.addMemberUser(User.createUserItemBuilder(user));
			// 群Id
			responseBuilder.setGroupId(group.getGroupId() + "");
			// groupItem.setGroupId(group.getGroupId() + "");
			// 群名
			responseBuilder.setGroupName(group.getGroupName());
			// groupItem.setGroupName(group.getGroupName());
			// responseBuilder.setGroupItem(groupItem);

			responseBuilder.setResultCode(GetGroupInfoRsp.ResultCode.SUCCESS);

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			logger.error(e.toString());
		} catch (MyException e) {
			logger.error(e.toString());
		}
		// 回复客户端说修改成功(保存在服务器成功)
		serverNetwork.sendToClient(new WaitClientResponse(networkPacket.ioSession, new PacketFromServer(networkPacket
				.getMessageID(), ProtoHead.ENetworkMessage.GET_GROUP_INFO_RSP_VALUE, responseBuilder.build().toByteArray())));
	}

	public Group getGroupInfo(int groupId, Session session) throws NoIpException {
		// 获取群资料
		ResultCode resultCode = ResultCode.NULL;
		List<Group> groupList = HibernateDataOperation.query(Group.GROUP_ID, groupId, Group.class, resultCode, session);

		Group group = null;
		if (resultCode.getCode() == ResultCode.SUCCESS && groupList.size() > 0)
			group = groupList.get(0);

		return group;
	}

	/**
	 * 修改群成员
	 * 
	 * @param networkPacket
	 * @throws NoIpException
	 * @author Feng
	 */
	public void changGroupChattingMember(NetworkPacket networkPacket) throws NoIpException {
		logger.debug("Server_Chatting : changGroupChattingMember : User ChangeGroupChattingMember Event :Deal with user's "
				+ ServerModel.getIoSessionKey(networkPacket.ioSession) + "  request");

		// 构造回复对象
		ChangeGroupChatMemberRsp.Builder responseBuilder = ChangeGroupChatMemberRsp.newBuilder();
		responseBuilder.setResultCode(ChangeGroupChatMemberRsp.ResultCode.FAIL);

		ChangeGroupChatMemberRsq changeGroupMemberObj;
		try {
			changeGroupMemberObj = ChangeGroupChatMemberRsq.parseFrom(networkPacket.getMessageObjectBytes());

			// 获取群聊名单
			Session session = HibernateSessionFactory.getSession();
			ResultCode resultCode = ResultCode.NULL;
			Group group = getGroupInfo(changeGroupMemberObj.getGroupId(), session);

			// 获取要处理的用户名单
			List<String> userList = changeGroupMemberObj.getUserIdList();

			ClientUser requestUser = serverModel.getClientUserFromTable(networkPacket.ioSession);
			// 检查权限 (在群用户中)
			if (requestUser == null || requestUser.userId == null || requestUser.userId.equals("")) {
				responseBuilder.setResultCode(ChangeGroupChatMemberRsp.ResultCode.NO_AUTHORITY);

				throw new MyException(
						"Server_Chatting : changGroupChattingMember(NetworkPacket) : User has no authority to Change member!");
			}
			boolean containsUser = false;
			for (User user : group.getMemberList())
				if (user.getUserId().equals(requestUser.userId)) {
					containsUser = true;
					break;
				}
			if (!containsUser)
				throw new MyException(
						"Server_Chatting : changGroupChattingMember(NetworkPacket) : User has no authority to Change member!");

			// 操作类型
			boolean containes;
			ArrayList<String> newUserList = new ArrayList<String>();
			List<User> newUserList2 = new ArrayList<User>();
			List<User> groupMemberList = group.getMemberList();
			if (changeGroupMemberObj.getChangeType() == ChangeType.ADD) { // 添加新用户
				// 整理出未存在群内的用户
				for (String userId : userList) {
					containes = false;
					for (User user : groupMemberList)
						if (user.getUserId().equals(userId)) {
							containes = true;
							break;
						}
					if (!containes)
						newUserList.add(userId);
				}
				if (newUserList.size() > 0) {
					newUserList2 = server_User.getUsers(newUserList, session);
					groupMemberList.addAll(newUserList2);
				}

			} else if (changeGroupMemberObj.getChangeType() == ChangeType.DELETE) { // 删除自己
				newUserList2.add(server_User.getUser(requestUser.userId, session));
				groupMemberList.remove(server_User.getUser(requestUser.userId, session));
			} else if (changeGroupMemberObj.getChangeType() == ChangeType.UPDATE) { // 修改群资料
				String newGroupName = changeGroupMemberObj.getGroupName();
				if (newGroupName != null && !newGroupName.equals(""))
					group.setGroupName(newGroupName);
			}

			// 存入数据库
			if (groupMemberList.size() == 0) { // 如果群已经空了，则删除群
				HibernateDataOperation.delete(group, resultCode);
			} else { // 否则，更新群
				HibernateDataOperation.update(group, resultCode, session);
				if (resultCode.getCode() == ResultCode.SUCCESS)
					logger.debug("Server_Chatting : changGroupChattingMember(NetworkPacket) : Change member successful!");
				else
					throw new MyException("Server_Chatting : changGroupChattingMember : Update to database Error!");
			}
			HibernateSessionFactory.commitSession(session);
			// 设置标志位
			responseBuilder.setResultCode(ChangeGroupChatMemberRsp.ResultCode.SUCCESS);
			serverNetwork.sendToClient(new WaitClientResponse(networkPacket.ioSession, new PacketFromServer(networkPacket
					.getMessageID(), ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER_RSP_VALUE, responseBuilder.build()
					.toByteArray())));
			
			// 通知所有用户
			ChangeGroupChatMemberSync.Type type;
			if (changeGroupMemberObj.getChangeType() == ChangeType.ADD)
				type = ChangeGroupChatMemberSync.Type.ADD;
			else if(changeGroupMemberObj.getChangeType() == ChangeType.DELETE)
				type = ChangeGroupChatMemberSync.Type.DELETE;
			else
				type = ChangeGroupChatMemberSync.Type.REFRESH;
			
			// 通知每位群成员修改列表
			sendChangeGroupSync(group, newUserList2, type);
			// 消息通知成员
			notifyMemberJionIn2(requestUser.userId, userList, group.getGroupId()+"");
			
			return;
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			logger.error(e.toString());
		} catch (MyException e) {
			logger.error(e.toString());
		}
		// 回复客户端
		serverNetwork.sendToClient(new WaitClientResponse(networkPacket.ioSession, new PacketFromServer(networkPacket
				.getMessageID(), ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER_RSP_VALUE, responseBuilder.build()
				.toByteArray())));
		
		// 显示“A已经要清B，C加入
		
	}
	
	/**
	 * 发送消息“（邀请者）已邀请（被邀请者）加入
	 * @param inviterUserId
	 * @param invitees
	 */
	private void notifyMemberJionIn1(String inviterUserId, List<User> inviteeUsers, String groupId) {
		String[] userList = new String[inviteeUsers.size()];
		for (int i=0; i<inviteeUsers.size(); i++)
			userList[i]= inviteeUsers.get(i).getUserId(); 
		notifyMemberJionIn(inviterUserId, userList, groupId);
	}
	private void notifyMemberJionIn2(String inviterUserId, List<String> inviteeUserIds, String groupId) {
		String[] userList = new String[inviteeUserIds.size()];
		inviteeUserIds.toArray(userList);
		notifyMemberJionIn(inviterUserId, userList, groupId);
	}
	private void notifyMemberJionIn(String inviterUserId, String[] inviteeUserIds, String groupId) {
		StringBuffer message = new StringBuffer("我已邀请");
		for (String userId : inviteeUserIds)
			message.append("'" + userId + "',");
		String message2 = message.subSequence(0, message.length()-1) + " 加入";
		
		ChatItem.Builder builder = ChatItem.newBuilder();
		builder.setChatBody(message2);
		builder.setChatType(ChatType.TEXT);
		builder.setSendUserId(inviterUserId);
		builder.setReceiveUserId(groupId);
		builder.setTargetType(TargetType.GROUP);
		
		try {
			clientSendChatting_Group(builder.build());
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}

	/**
	 * 给用户发送群人员变更消息
	 * 
	 * @param userList
	 * @param type
	 */
	private void sendChangeGroupSync(Group group, List<User> userList, ChangeGroupChatMemberSync.Type type) {
		User[] userList2 = new User[userList.size()];
		userList.toArray(userList2);
		sendChangeGroupSync(group, userList2, type);
	}
	private void sendChangeGroupSync(Group group, User[] userList, ChangeGroupChatMemberSync.Type type) {
		ChangeGroupChatMemberSync.Builder builder = ChangeGroupChatMemberSync.newBuilder();
		builder.setType(type);

		for (User user : userList)
			builder.addUserId(user.getUserId());
		
		ClientUser clientUser;
		// 对每个在线的群成员发送
		for (User user : group.getMemberList()) {
			clientUser = serverModel.getClientUserByUserId(user.getUserId());
			if (clientUser == null)
				continue;
			
			serverNetwork.sendToClient(clientUser.ioSession, new PacketFromServer(
					ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER_SYNC_VALUE, builder.build().toByteArray()));
		}
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
