package tools;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * MyHttpServer 实现一个简单的HTTP服务器端，可以获取用户提交的内容 并给用户一个response
 * 因为时间的关系，对http头的处理显得不规范 对于上传附件，暂时只能解析只上传一个附件而且附件位置在第一个的情况
 * 转载请注明来自http://blog.csdn.net/sunxing007
 * **/
public class HttpServer2 {
	// 服务器根目录，post.html, upload.html都放在该位置
	public static String WEB_ROOT = "c:/root";
	// 端口
	private int port;
	// 用户请求的文件的url
	private String requestPath = "http://192.168.45.17";
	// mltipart/form-data方式提交post的分隔符,
	private String boundary = null;
	// post提交请求的正文的长度
	private int contentLength = 0;

	public HttpServer2(String root, int port) {
		WEB_ROOT = root;
		this.port = port;
//		requestPath = null;
	}

	// 处理GET请求
	private void doGet(DataInputStream reader, OutputStream out) throws Exception {
		if (new File(WEB_ROOT + this.requestPath).exists()) {
			// 从服务器根目录下找到用户请求的文件并发送回浏览器
			InputStream fileIn = new FileInputStream(WEB_ROOT + this.requestPath);
			byte[] buf = new byte[fileIn.available()];
			fileIn.read(buf);
			out.write(buf);
			out.close();
			fileIn.close();
			reader.close();
			System.out.println("request complete.");
		}
	}

	// 处理post请求
	private void doPost(DataInputStream reader, OutputStream out) throws Exception {
		String line = reader.readLine();
		while (line != null) {
			System.out.println(line);
			line = reader.readLine();
			if ("".equals(line)) {
				break;
			} else if (line.indexOf("Content-Length") != -1) {
				this.contentLength = Integer.parseInt(line.substring(line.indexOf("Content-Length") + 16));
			}
			// 表明要上传附件， 跳转到doMultiPart方法。
			else if (line.indexOf("multipart/form-data") != -1) {
				// 得multiltipart的分隔符
				this.boundary = line.substring(line.indexOf("boundary") + 9);
				this.doMultiPart(reader, out);
				return;
			}
		}
		// 继续读取普通post（没有附件）提交的数据
		System.out.println("begin reading posted data......");
		String dataLine = null;
		
		// 用户发送的post数据正文
		byte[] buf = {};
		int size = 0;
		ArrayList<RequestData> requestDataList = new ArrayList<RequestData>();
		if (this.contentLength != 0) {
			buf = new byte[this.contentLength];
			while (size < this.contentLength) {
				int c = reader.read();
				buf[size++] = (byte) c;

			}
			String data = new String(buf, 0, size);
			System.out.println("The data user posted: " + new String(buf, 0, size));
			RequestData.analyseStr(requestDataList, data);
		}
		
		// 分析客户端需求
		String value = RequestData.getKeyValue(requestDataList, "type");
		if (value != null && value.equals("Image")) {
//			value = RequestData.getKeyValue(requestDataList, "imageType");
//			if (value != null && value.equals("jpg")) {
//			}
			value = RequestData.getKeyValue(requestDataList, "imageName");
			if (value != null) {
				System.out.println("输出图片!");
				byte[] fileBytes = DataTypeTranslater.fileToByte("D:/" + value);
				out.write(getImageResponse(fileBytes).getBytes());
				out.write(fileBytes);
				out.flush();
				out.close();
				System.out.println("输出图片完毕!");
			}
		}
		
		
		// 发送回浏览器的内容
//		String response = "";
//		response += "HTTP/1.1 200 OK/n";
//		response += "Server: Sunpache 1.0/n";
//		response += "Content-Type: text/html/n";
//		response += "Last-Modified: Mon, 11 Jan 1998 13:23:42 GMT/n";
//		response += "Accept-ranges: bytes";
//		response += "/n";
//		String body = "<html><head><title>test server</title></head><body><p>post ok:</p>" + new String(buf, 0, size)
//				+ "</body></html>";
//		System.out.println(body);
//		out.write(response.getBytes());
//		out.write(body.getBytes());
//		out.flush();
//		reader.close();
//		out.close();
		System.out.println("request complete.");
	}

	// 处理附件
	private void doMultiPart(DataInputStream reader, OutputStream out) throws Exception {
		System.out.println("doMultiPart ......");
		String line = reader.readLine();
		while (line != null) {
			System.out.println(line);
			line = reader.readLine();
			if ("".equals(line)) {
				break;
			} else if (line.indexOf("Content-Length") != -1) {
				this.contentLength = Integer.parseInt(line.substring(line.indexOf("Content-Length") + 16));
				System.out.println("contentLength: " + this.contentLength);
			} else if (line.indexOf("boundary") != -1) {
				// 获取multipart分隔符
				this.boundary = line.substring(line.indexOf("boundary") + 9);
			}
		}
		System.out.println("begin get data......");
		/*
		 * 下面的注释是一个浏览器发送带附件的请求的全文，所有中文都是说明性的文字***** <HTTP头部内容略> ............
		 * Cache-Control: no-cache <这里有一个空行，表明接下来的内容都是要提交的正文>
		 * -----------------------------7d925134501f6<这是multipart分隔符>
		 * Content-Disposition: form-data; name="myfile"; filename="mywork.doc"
		 * Content-Type: text/plain
		 * 
		 * <附件正文>........................................
		 * .................................................
		 * 
		 * -----------------------------7d925134501f6<这是multipart分隔符>
		 * Content-Disposition: form-data; name="myname"<其他字段或附件> <这里有一个空行>
		 * <其他字段或附件的内容>
		 * -----------------------------7d925134501f6--<这是multipart分隔符
		 * ，最后一个分隔符多两个->
		 * **************************************************************
		 */
		/**
		 * 上面的注释是一个带附件的multipart类型的POST的全文模型， 要把附件去出来，就是要找到附件正文的起始位置和结束位置
		 * **/
		if (this.contentLength != 0) {
			// 把所有的提交的正文，包括附件和其他字段都先读到buf.
			byte[] buf = new byte[this.contentLength];
			int totalRead = 0;
			int size = 0;
			while (totalRead < this.contentLength) {
				size = reader.read(buf, totalRead, this.contentLength - totalRead);
				totalRead += size;
			}
			// 用buf构造一个字符串，可以用字符串方便的计算出附件所在的位置
			String dataString = new String(buf, 0, totalRead);
			System.out.println("the data user posted:/n" + dataString);
			int pos = dataString.indexOf(boundary);
			// 以下略过4行就是第一个附件的位置
			pos = dataString.indexOf("/n", pos) + 1;
			pos = dataString.indexOf("/n", pos) + 1;
			pos = dataString.indexOf("/n", pos) + 1;
			pos = dataString.indexOf("/n", pos) + 1;
			// 附件开始位置
			int start = dataString.substring(0, pos).getBytes().length;
			pos = dataString.indexOf(boundary, pos) - 4;
			// 附件结束位置
			int end = dataString.substring(0, pos).getBytes().length;
			// 以下找出filename
			int fileNameBegin = dataString.indexOf("filename") + 10;
			int fileNameEnd = dataString.indexOf("/n", fileNameBegin);
			String fileName = dataString.substring(fileNameBegin, fileNameEnd);
			/**
			 * 有时候上传的文件显示完整的文件名路径,比如c:/my file/somedir/project.doc
			 * 但有时候只显示文件的名字，比如myphoto.jpg. 所以需要做一个判断。
			 */
			if (fileName.lastIndexOf("//") != -1) {
				fileName = fileName.substring(fileName.lastIndexOf("//") + 1);
			}
			fileName = fileName.substring(0, fileName.length() - 2);
			OutputStream fileOut = new FileOutputStream("c://" + fileName);
			fileOut.write(buf, start, end - start);
			fileOut.close();
			fileOut.close();
		}
		String response = "";
		response += "HTTP/1.1 200 OK/n";
		response += "Server: Sunpache 1.0/n";
		response += "Content-Type: text/html/n";
		response += "Last-Modified: Mon, 11 Jan 1998 13:23:42 GMT/n";
		response += "Accept-ranges: bytes";
		response += "/n";
		out.write("<html><head><title>test server</title></head><body><p>Post is ok</p></body></html>".getBytes());
		out.flush();
		reader.close();
		System.out.println("request complete.");
	}

	public void service() throws Exception {
		ServerSocket serverSocket = new ServerSocket(this.port);
		System.out.println("server is ok.");
		// 开启serverSocket等待用户请求到来，然后根据请求的类别作处理
		// 在这里我只针对GET和POST作了处理
		// 其中POST具有解析单个附件的能力
		while (true) {
			Socket socket = serverSocket.accept();
			System.out.println("new request coming.");
			
			DataInputStream reader = new DataInputStream((socket.getInputStream()));
			String line = reader.readLine();
			String method = line.substring(0, 4).trim();
			
			OutputStream out = socket.getOutputStream();
			this.requestPath = line.split(" ")[1];
			System.out.println(method);
			if ("GET".equalsIgnoreCase(method)) {
				System.out.println("do get......");
				this.doGet(reader, out);
			} else if ("POST".equalsIgnoreCase(method)) {
				System.out.println("do post......");
				this.doPost(reader, out);
			}
			socket.close();
			System.out.println("socket closed.");
		}
	}

	public static void main(String args[]) throws Exception {
		HttpServer2 server = new HttpServer2("D:/", 8080);
		server.service();
	}

	/**
	 * 获取图像类型的回复
	 * @param fileBytes
	 * @return
	 */
	public static String getImageResponse(byte[] fileBytes) {
		String response = "";
		response += "Content-Type=image/gif; charset=utf-8\n";
		response += "Content-Length=" + Integer.toString(fileBytes.length);
		response += "/n";

//		IoBuffer responseIoBuffer = IoBuffer.allocate(fileBytes.length);
//		responseIoBuffer.put(fileBytes);
//		responseIoBuffer.flip();

		return response;
	}
}
class RequestData {
	public String requestKey, requestValue;
	
	public RequestData(String requestKey, String requestValue) {
		this.requestKey = requestKey;
		this.requestValue = requestValue;
	}
	
	/**
	 *  分析字符串，并把结果加入列表
	 * @param requestDataList
	 * @param string
	 */
	public static void analyseStr(ArrayList<RequestData> requestDataList, String string) {
		String[] data1 = string.split("&");
		String [] data3;
		for (String data2 : data1) {
			data3 = data2.split("=");
			requestDataList.add(new RequestData(data3[0], data3[1]));
		}
	}
	
	/**
	 *  返回某key的value
	 * @param requestDataList
	 * @param key
	 * @return
	 */
	public static String getKeyValue(ArrayList<RequestData> requestDataList, String key) {
		for (RequestData rd : requestDataList)
			if (rd.requestKey.equals(key))
				return rd.requestValue;
		
		return null;
	}
}
