package server;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;

/**
 *  用switch进行请求分发
 * @author Feng
 *
 */
public class ClientRequest_Dispatcher {
	public static ClientRequest_Dispatcher instance = new ClientRequest_Dispatcher();

	private ClientRequest_Dispatcher() {

	}

	/**
	 *  根据请求的类型分配给不同的处理器
	 * @param networkMessage
	 * @author Feng
	 */
	public void dispatcher(NetworkMessage networkMessage) {
//		System.out.println("IP" + networkMessage.ioSession.getRemoteAddress());
		System.out.println("ClientRequest_Dispatcher: Client的请求类型是 " + networkMessage.getMessageType().toString());
		
		switch (networkMessage.getMessageType().getNumber()) {
		// Client回复心跳包
		case ProtoHead.ENetworkMessage.KeepAliveSync_VALUE:
			Server_User.instance.KeepAlive(networkMessage);
			break;
		case ProtoHead.ENetworkMessage.RegisterReq_VALUE:

			break;
		case ProtoHead.ENetworkMessage.LoginReq_VALUE:

			break;

		default:
			break;
		}
	}
}
