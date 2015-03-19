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
//		System.out.println((networkMessage == null) + "      " + (networkMessage.ioSession == null));
		System.out.println("Server_User: 对  用户" + networkMessage.ioSession.getRemoteAddress() + "  回复的心跳包  的处理");
//		System.out.println(ServerModel.instance.clientUserTable.keySet().size());
		System.out.println("fuck   " + ServerModel.instance.clientUserTable.containsKey(networkMessage.ioSession.getRemoteAddress().toString()));
		// 如果ClientUser已经掉线被删除，那么就不管了
		try {
			if (!ServerModel.instance.clientUserTable.containsKey(networkMessage.ioSession.getRemoteAddress().toString())){
				System.out.println("Server_User: 用户表(ClientUserTalbe)中找不到 用户" + networkMessage.ioSession.getRemoteAddress() + "，心跳回复不作处理!");
				return;
			}
			System.err.println("a");
			
			ServerModel.instance.clientUserTable.get(networkMessage.ioSession.getRemoteAddress().toString()).onLine = true;
		} catch (NullPointerException e) {
			System.out.println("Server_User: 异常，用户" + networkMessage.ioSession.getRemoteAddress() + "已掉线，心跳回复不作处理!");
			e.printStackTrace();
		}
	}
	
	// 处理新用户注册事件
	public void Register() {
		
	}
	
}
