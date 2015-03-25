package server;

import java.util.Date;

import org.apache.mina.core.session.IoSession;

/**
 * 这是一个存在“监听Client回复”表中的对象
 * @author Feng
 *
 */
public class WaitClientResponse {
	long time;
	public byte[] messageHasSent;
	public IoSession ioSession;
	
	public WaitClientResponse(IoSession ioSession, byte[] messageHasSent) {
		this.ioSession = ioSession;
		this.messageHasSent = messageHasSent;
		
		time = new Date().getTime();
	}
}
