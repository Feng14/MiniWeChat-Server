package server;

import com.google.protobuf.InvalidProtocolBufferException;

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
	public static ClientRequest_Dispatcher instance = new ClientRequest_Dispatcher();

	private ClientRequest_Dispatcher() {

	}

	/**
	 * 根据请求的类型分配给不同的处理器
	 * 
	 * @param networkMessage
	 * @author Feng
	 */
	public void dispatcher(NetworkMessage networkMessage) {
		// System.out.println("IP" +
		// networkMessage.ioSession.getRemoteAddress());
		Debug.log("ClientRequest_Dispatcher", "Client的请求类型是 " + networkMessage.getMessageType().toString());

		try {
			switch (networkMessage.getMessageType().getNumber()) {
			// Client回复心跳包
			case ProtoHead.ENetworkMessage.KEEP_ALIVE_SYNC_VALUE:
				Server_User.instance.keepAlive(networkMessage);
				break;
			case ProtoHead.ENetworkMessage.REGISTER_REQ_VALUE:
				Server_User.instance.register(networkMessage);
				break;
			case ProtoHead.ENetworkMessage.LOGIN_REQ_VALUE:
				Server_User.instance.login(networkMessage);
				break;
			case ProtoHead.ENetworkMessage.PERSONALSETTINGS_REQ_VALUE:
				Server_User.instance.personalSettings(networkMessage);
				break;
			case ProtoHead.ENetworkMessage.GET_USERINFO_REQ_VALUE:
				Server_Friend.instance.getUserInfo(networkMessage);
				break;
			case ProtoHead.ENetworkMessage.ADD_FRIEND_REQ_VALUE:
				Server_Friend.instance.addFriend(networkMessage);
				break;
			case ProtoHead.ENetworkMessage.DELETE_FRIEND_REQ_VALUE:
				Server_Friend.instance.deleteFriend(networkMessage);
				break;
			// 另一个人登陆，本用户被踢下的通知的回复
			case ProtoHead.ENetworkMessage.OFFLINE_SYNC_VALUE:
				Server_User.instance.clientOfflineResponse(networkMessage);
				break;
			case ProtoHead.ENetworkMessage.LOGOUT_REQ_VALUE:
				Server_User.instance.logout(networkMessage);
				break;
			case ProtoHead.ENetworkMessage.GET_PERSONALINFO_REQ_VALUE:
				Server_User.instance.getPersonalInfo(networkMessage);
				break;

			default:
				break;
			}
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}
}
