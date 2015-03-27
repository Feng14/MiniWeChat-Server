package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.http.HttpServerCodec;
import org.apache.mina.http.api.DefaultHttpResponse;
import org.apache.mina.http.api.HttpRequest;
import org.apache.mina.http.api.HttpResponse;
import org.apache.mina.http.api.HttpStatus;
import org.apache.mina.http.api.HttpVersion;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class HttpServer {

	public static void main(String[] args) throws IOException {
		IoAcceptor acceptor = new NioSocketAcceptor();
		acceptor.getFilterChain().addLast("codec", new HttpServerCodec());
		acceptor.setHandler(new HttpServerHandle());
		acceptor.bind(new InetSocketAddress(8080));
	}
}

class HttpServerHandle extends IoHandlerAdapter {

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		Debug.log("HttpServer", "exceptionCaught");
		cause.printStackTrace();
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		Debug.log("HttpServer", "messageReceived");

		if (message instanceof HttpRequest) {

			// 请求，解码器将请求转换成HttpRequest对象
			HttpRequest request = (HttpRequest) message;

			// 获取请求参数
			String name = request.getParameter("name");
			System.out.println("name : " + name);
			System.out.println(request.getQueryString());
			System.out.println(request.getRequestPath());


//			Token token = (Token)message;
			System.out.println(request.getParameters().size());;
			
//			System.out.println(request.toString());
//			System.out.println("1111111111111111111111111111111111111111");
			
//			System.out.println(token.getBusiMessage().toString());
			
			
			
			String data = request.getParameter("data");
			System.out.println("data : " + data);
			try {
				name = URLDecoder.decode(name, "UTF-8");
			} catch (Exception e) {
				System.err.println("HttpServer : 解码异常!");
			}

			// 响应HTML
			// byte[] responseBytes = getHelloResponse(name);
			byte[] responseBytes = getImageResonse(name);

			int contentLength = responseBytes.length;

			// 构造HttpResponse对象，HttpResponse只包含响应的status line和header部分
//			headers.put("Content-Type", "text/html; charset=utf-8");
			HttpResponse response = getImageHttpResponse(contentLength);

			// 响应BODY
			IoBuffer responseIoBuffer = IoBuffer.allocate(contentLength);
			responseIoBuffer.put(responseBytes);
			responseIoBuffer.flip();

			session.write(response); // 响应的status line和header部分
			session.write(responseIoBuffer); // 响应body部分
		}
	}

	public byte[] getHelloResponse(String name) throws UnsupportedEncodingException {
		// 生成HTML
		String responseHtml = "<html><body>Hello, " + name + "</body></html>";
		return responseHtml.getBytes("UTF-8");
	}

	public byte[] getImageResonse(String name) throws IOException {
//		String filePath = "D:/2.jpg";
		String filePath = "D:/" + name;
		System.err.println("FilePath : " + filePath);
		
		File file = new File(filePath);
		long fileSize = file.length();

		FileInputStream fi = new FileInputStream(file);
		byte[] buffer = new byte[(int) fileSize];
		
		int offset = 0;
		int numRead = 0;
		while (offset < buffer.length && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
			offset += numRead;
		}
		// 获取字节数组长度
		if (offset != buffer.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}
		fi.close();
		return buffer;
	}
	
	public HttpResponse getImageHttpResponse(int length){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "image/gif; charset=utf-8");
		headers.put("Content-Length", Integer.toString(length));
		return new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SUCCESS_OK, headers);
		
	}
}
