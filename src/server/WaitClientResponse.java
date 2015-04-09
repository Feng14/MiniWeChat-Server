package server;

import java.util.Date;

import org.apache.mina.core.session.IoSession;

/**
 * 这是一个存在“监听Client回复”表中的对象
 * @author Feng
 *
 */
public class WaitClientResponse {
//	long time;
//	public byte[] messageHasSent;
	public PacketFromServer packetFromServer;
	public IoSession ioSession;
	public WaitClientResponseCallBack waitClientResponseCallBack;
	
	public WaitClientResponse(IoSession ioSession, PacketFromServer packetFromServer) {
		this.ioSession = ioSession;
		this.packetFromServer = packetFromServer;
		this.waitClientResponseCallBack = null;
	}
	
	public WaitClientResponse(IoSession ioSession, PacketFromServer packetFromServer, WaitClientResponseCallBack waitClientResponseCallBack) {
		this.ioSession = ioSession;
		this.packetFromServer = packetFromServer;
		this.waitClientResponseCallBack = waitClientResponseCallBack;
	}
}
