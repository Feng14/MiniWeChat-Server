package server_HTTP;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import com.sun.xml.internal.messaging.saaj.packaging.mime.Header;

public class MyHttpServer implements HttpHandler {
	private Map<String, HttpHandler> contextMap = new HashMap<String, HttpHandler>();
	public String contextPath = "";

	public static void main(String args[]) {
		new MyHttpServer();
	}

	public MyHttpServer() {
		try {
			// 允许最大连接数
			int backLog = 10;
			InetSocketAddress inetSock = new InetSocketAddress(8086);
			HttpServer httpServer = HttpServer.create(inetSock, backLog);
			// 直接返回Hello.....
			// httpServer.createContext("/", new HandlerTestA());
			// 显示已经处理的请求数，采用线程池
			httpServer.createContext("/", this);
			httpServer.setExecutor(null);
			httpServer.start();
			System.out.println("HttpServer Test Start!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		System.out.println("fuck");
		System.out.println(exchange.getRequestURI());
		System.out.println(exchange.getRemoteAddress());
		System.out.println(exchange.getRequestBody().toString());
		System.out.println("shit");
		System.out.println(exchange.getRequestMethod());
		System.out.println(exchange.getRequestHeaders().size());
	}
}
