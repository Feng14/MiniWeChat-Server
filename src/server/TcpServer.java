package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class TcpServer {
	public static void main(String[] args) throws IOException {
		// 显示IP地址
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			System.out.println("IP地址：" + addr.getHostAddress().toString());
			System.out.println("本机名称：" + addr.getHostName().toString());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		System.out.println("端口号：8080");
		
		IoAcceptor acceptor = new NioSocketAcceptor();
		acceptor.setHandler(new TcpServerHandle());
		acceptor.bind(new InetSocketAddress(8080));
	}

}

class TcpServerHandle extends IoHandlerAdapter {
	private int count = 0;

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		cause.printStackTrace();
	}

	// 接收到新的数据
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {

		// 接收客户端的数据
		IoBuffer ioBuffer = (IoBuffer) message;
		byte[] byteArray = new byte[ioBuffer.limit()];
		ioBuffer.get(byteArray, 0, ioBuffer.limit());
		System.out.println("messageReceived:" + new String(byteArray, "UTF-8"));

		// 发送到客户端
//		byte[] responseByteArray = "Hello MiniWeChat Client".getBytes("UTF-8");
		byte[] responseByteArray = "Hello".getBytes("UTF-8");
//		byte[] responseByteArray = new byte[].getBytes("UTF-8");
//		byte[] responseByteArray = "Fuck 王选易".getBytes("UTF-8");
		IoBuffer responseIoBuffer = IoBuffer.allocate(responseByteArray.length);
		responseIoBuffer.put(responseByteArray);
		responseIoBuffer.flip();
		session.write(responseIoBuffer);
		System.out.println("发送完毕！！");
	}

	// 由底层决定是否创建一个session
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		System.out.println("sessionCreated");
	}

	// 创建了session 后会回调sessionOpened
	public void sessionOpened(IoSession session) throws Exception {
		count++;
		System.out.println("第 " + count + " 个 client 登陆！address： : " + session.getRemoteAddress());
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println("sessionClosed");
		
	}
}