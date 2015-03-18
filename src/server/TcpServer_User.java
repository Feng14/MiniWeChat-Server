package server;

import org.apache.mina.core.session.IoSession;

import protocol.KeepAliveMsg;
import protocol.ProtoHead;

// 主服务器下的自服务器，负责处理用户相关事件
public class TcpServer_User {
	public static TcpServer_User instance = new TcpServer_User();
	
	private TcpServer_User(){
		
	}
	
	// 用户心跳包
	public void KeepAlive(IoSession session, int size, KeepAliveMsg.KeepAliveSyncPacket packet){
		
	}
	
	// 处理新用户注册事件
	public void Register() {
		
	}
	
}
