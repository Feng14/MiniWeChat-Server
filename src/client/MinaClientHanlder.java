package client;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

public class MinaClientHanlder extends IoHandlerAdapter {
	public void sessionOpened(IoSession session) throws Exception {
		System.out.println("客户端登陆");
		session.write("hello world");
	}

	public void sessionClosed(IoSession session) {
		System.out.println("client close");
	}

	public void messageReceived(IoSession session, Object message) throws Exception {
		System.out.println("客户端接受到了消息:" + message);
	}
}
// 运行结果 