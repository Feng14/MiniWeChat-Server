package server;

import org.apache.mina.core.session.IoSession;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;

// 主服务器下的子服务器，负责处理用户相关事件
public class Server_User {
	public static Server_User instance = new Server_User();
	
	private Server_User(){
		
	}
	
	/**
	 *  对  用户心跳包回复  的处理
	 *  将online值设为True
	 * @param networkMessage
	 */
	public void KeepAlive(NetworkMessage networkMessage){
		System.out.println("Server_User: 对  用户" + networkMessage.ioSession.getRemoteAddress() + "的心跳包回复  的处理");
//		System.out.println(ServerModel.instance.clientUserTable.keySet().size());
		System.out.println("fuck   " + networkMessage== null);
		// 如果ClientUser已经掉线被删除，那么就不管了
		if (!ServerModel.instance.clientUserTable.containsKey(networkMessage.ioSession.getRemoteAddress())){
			System.out.println("Server_User: 用户" + networkMessage.ioSession.getRemoteAddress() + "已掉线，心跳回复不作处理!");
			return;
		}
		
		ServerModel.instance.clientUserTable.get(networkMessage.ioSession.getRemoteAddress()).onLine = true;
	}
	
	// 处理新用户注册事件
	public void Register() {
		
	}
	
}
