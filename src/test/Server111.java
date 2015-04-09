package test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import server.MinaDecoder;
import server.MinaEncoder;
import server.ServerNetwork;
import tools.Debug;

public class Server111 extends IoHandlerAdapter {
//	public static Server111 instance = new Server111();

	private InetSocketAddress inetSocketAddress;
	private IoAcceptor acceptor;
	
	public static void main(String args[]) {
		new Server111();
	}
	
	public Server111(){
		// 显示IP地址
				InetAddress addr;
				try {
					addr = InetAddress.getLocalHost();
					Debug.log("IP address:" + addr.getHostAddress().toString());
					Debug.log("Host Name:" + addr.getHostName().toString());
					// logger.debug("IP地址:"+addr.getHostAddress().toString());
					// logger.debug("本机名称:"+ addr.getHostName().toString());

				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
				Debug.log("Port Number：8081");
				// logger.debug("端口号：8081");

				acceptor = new NioSocketAcceptor();
				// 指定编码解码器
				acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MinaEncoder(), new MinaDecoder()));
				acceptor.setHandler(this);
				try {
					acceptor.bind(new InetSocketAddress(8081));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		System.out.println("fucking");
		CloseFuture future = session.close(false);
		future.addListener(new IoFutureListener<IoFuture>() {
			@Override
			public void operationComplete(IoFuture future) {
//				Calendar calendar = Calendar.getInstance();
//				System.out.println(calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND));
//				System.out.println(future.isDone());
			}
		});
	}

	public void sessionOpened(IoSession session) throws Exception {
		System.out.println(session.isClosing());
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println(session.isConnected());
		System.out.println(acceptor.getStatistics());
	}
	
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub

//		super.messageSent(session, message);
		
//		System.out.println(message);
//		printStackTrace(Server111.class);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		Debug.log("throws exception");
		Debug.log("session.toString()", session.toString());
		Debug.log("cause.toString()", cause.toString());
		Debug.log("Report Error Over!!");
	}

}
