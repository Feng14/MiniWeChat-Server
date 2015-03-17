package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class MinaServer {
	public static void main(String[] args) {
		// 显示IP地址
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			System.out.println("IP地址：" + addr.getHostAddress().toString());
			System.out.println("本机名称：" + addr.getHostName().toString());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		
		// 创建ServerScoket
		SocketAcceptor acceptor = new NioSocketAcceptor();
		// 设置传输方式（这里设置成对象传输模式，还有很多的类型后面会具体讲到
		DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
		ProtocolCodecFilter filter = new ProtocolCodecFilter(new ObjectSerializationCodecFactory());
		chain.addLast("objectFilter", filter);
		// 添加消息处理

		acceptor.setHandler(new MinaServerHanlder());
		// 开启服务器
		int bindPort = 9988;
		try {
			acceptor.bind(new InetSocketAddress(bindPort));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
