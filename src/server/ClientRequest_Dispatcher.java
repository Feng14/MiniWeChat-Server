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
	public void dispatcher(PacketFromClient packetFromClient) {
		// System.out.println("IP" +
		// networkMessage.ioSession.getRemoteAddress());
		Debug.log("ClientRequest_Dispatcher", "Client's request type is : " + packetFromClient.getMessageType().toString());

		try {
			switch (packetFromClient.getMessageType().getNumber()) {
			// Client回复心跳包
			case ProtoHead.ENetworkMessage.KEEP_ALIVE_SYNC_VALUE:
				System.out.println("now");
				server_User.keepAlive(packetFromClient);
				break;
			case ProtoHead.ENetworkMessage.REGISTER_REQ_VALUE:
				server_User.register(packetFromClient);
				break;
			case ProtoHead.ENetworkMessage.LOGIN_REQ_VALUE:
				server_User.login(packetFromClient);
				break;
			case ProtoHead.ENetworkMessage.PERSONALSETTINGS_REQ_VALUE:
				server_User.personalSettings(packetFromClient);
				break;
			case ProtoHead.ENetworkMessage.GET_USERINFO_REQ_VALUE:
				server_Friend.getUserInfo(packetFromClient);
				break;
			case ProtoHead.ENetworkMessage.ADD_FRIEND_REQ_VALUE:
				server_Friend.addFriend(packetFromClient);
				break;
			case ProtoHead.ENetworkMessage.DELETE_FRIEND_REQ_VALUE:
				server_Friend.deleteFriend(packetFromClient);
				break;
			// 另一个人登陆，本用户被踢下的通知的回复
			case ProtoHead.ENetworkMessage.OFFLINE_SYNC_VALUE:
				server_User.clientOfflineResponse(packetFromClient);
				break;
			case ProtoHead.ENetworkMessage.LOGOUT_REQ_VALUE:
				server_User.logout(packetFromClient);
				break;
			case ProtoHead.ENetworkMessage.GET_PERSONALINFO_REQ_VALUE:
				server_User.getPersonalInfo(packetFromClient);
				break;
			// client发送消息
			case ProtoHead.ENetworkMessage.SEND_CHAT_REQ_VALUE:
				server_Chatting.clientSendChatting(packetFromClient);
				break;
			// 服务器向客户端发送未接收消息，客户端的回答
			case ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE:
				server_Chatting.clientReceiveChatting(packetFromClient);
				break;
			default:
				break;
			}
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}
}
