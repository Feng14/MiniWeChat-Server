package server;

import org.apache.mina.core.session.IoSession;

/**
 * 服务器端对客户端用户的状态记录
 * @author Feng
 *
 */
public class ClientUser {
	public IoSession ioSession;
	public boolean onLine = true;
}
