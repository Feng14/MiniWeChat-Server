package server;

import exception.NoIpException;

import protocol.ProtoHead;
import tools.Debug;

/**
 * 用switch进行请求分发
 * 
 * @author Feng
 * 
 */
public class ClientRequest_Dispatcher {
	// public static ClientRequest_Dispatcher instance = new
	// ClientRequest_Dispatcher();
	private Server_User server_User;
	private Server_Friend server_Friend;
	private Server_Chatting server_Chatting;

	public Server_User getServer_User() {
		return server_User;
	}

	public void setServer_User(Server_User server_User) {
		this.server_User = server_User;
	}

	public Server_Friend getServer_Friend() {
		return server_Friend;
	}

	public void setServer_Friend(Server_Friend server_Friend) {
		this.server_Friend = server_Friend;
	}

	public Server_Chatting getServer_Chatting() {
		return server_Chatting;
	}

	public void setServer_Chatting(Server_Chatting server_Chatting) {
		this.server_Chatting = server_Chatting;
	}

	/**
	 * 根据请求的类型分配给不同的处理器
	 * 
	 * @param networkMessage
	 * @author Feng
	 */
	public void dispatcher(NetworkPacket networkPacket) {
		// System.out.println("IP" +
		// networkMessage.ioSession.getRemoteAddress());
		Debug.log("ClientRequest_Dispatcher", "Client's request type is : " + networkPacket.getMessageType().toString());

		try {
			switch (networkPacket.getMessageType().getNumber()) {
			// Client回复心跳包
//			case ProtoHead.ENetworkMessage.KEEP_ALIVE_SYNC_VALUE:
//				server_User.keepAlive(networkPacket);
//				break;
			case ProtoHead.ENetworkMessage.REGISTER_REQ_VALUE:
				server_User.register(networkPacket);
				break;
			case ProtoHead.ENetworkMessage.LOGIN_REQ_VALUE:
				server_User.login(networkPacket);
				break;
			case ProtoHead.ENetworkMessage.PERSONALSETTINGS_REQ_VALUE:
				server_User.personalSettings(networkPacket);
				break;
			case ProtoHead.ENetworkMessage.GET_USERINFO_REQ_VALUE:
				server_Friend.getUserInfo(networkPacket);
				break;
			case ProtoHead.ENetworkMessage.ADD_FRIEND_REQ_VALUE:
				server_Friend.addFriend(networkPacket);
				break;
			case ProtoHead.ENetworkMessage.DELETE_FRIEND_REQ_VALUE:
				server_Friend.deleteFriend(networkPacket);
				break;
			// 另一个人登陆，本用户被踢下的通知的回复
//			case ProtoHead.ENetworkMessage.OFFLINE_SYNC_VALUE:
//				server_User.clientOfflineResponse(networkPacket);
//				break;
			case ProtoHead.ENetworkMessage.LOGOUT_REQ_VALUE:
				server_User.logout(networkPacket);
				break;
			case ProtoHead.ENetworkMessage.GET_PERSONALINFO_REQ_VALUE:
				server_User.getPersonalInfo(networkPacket);
				break;
			// client发送消息
			case ProtoHead.ENetworkMessage.SEND_CHAT_REQ_VALUE:
				server_Chatting.clientSendChatting(networkPacket);
				break;
			// 服务器向客户端发送未接收消息，客户端的回答
//			case ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE:
//				server_Chatting.clientReceiveChatting(networkPacket);
//				break;
			// 创建群聊
			case ProtoHead.ENetworkMessage.CREATE_GROUP_CHAT_REQ_VALUE:
				server_Chatting.createGroupChatting(networkPacket);
				break;
			// 修改群聊成员
			case ProtoHead.ENetworkMessage.CHANGE_GROUP_CHAT_MEMBER_REQ_VALUE:
				server_Chatting.changGroupChattingMember(networkPacket);
				break;
			default:
				break;
			}
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}
}
