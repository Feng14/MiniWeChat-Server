package server;

import org.apache.mina.core.session.IoSession;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;

// 主服务器下的子服务器，负责处理用户相关事件
public class Server_User {
	public static Server_User instance = new Server_User();
	
	private Server_User(){
		
	}
	
	// 对  用户心跳包回复  的处理
	public void KeepAlive(NetworkMessage networkMessage){
		System.out.println(" 对  用户心跳包回复  的处理");
//		ServerModel.instance.clientUserSet[networkMessage.ioSession.getRemoteAddress()]
	}
	
	// 处理新用户注册事件
	public void Register() {
		
	}
	
}
