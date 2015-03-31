package tools;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import server_HTTP.ServletServer;

public class HttpClient {
	/**
	 * 向指定URL发送GET方法的请求
	 * 
	 * @param url
	 *            发送请求的URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return URL 所代表远程资源的响应结果
	 */
	public static String sendGet(String url, String param) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString = url + "?" + param;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际的连接
			connection.connect();
			// 获取所有响应头字段
			Map<String, List<String>> map = connection.getHeaderFields();
			// 遍历所有的响应头字段
			for (String key : map.keySet()) {
				System.out.println(key + "--->" + map.get(key));
			}
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送GET请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 向指定 URL 发送POST方法的请求
	 * 
	 * @param url
	 *            发送请求的 URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);

			// 发送图片
			OutputStream outputStream = conn.getOutputStream();

			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	public HttpClient() {

	}

	// public static void main(String args[]) {
	// // new HttpClient();
	// sendPost("http://192.168.45.17:8080/MiniWechat/TransferFile",
	// "type=Image&imageName=1.jpg");
	// }

	static String sessionId = "";

	public static String getPicBASE64() {
		String picPath = "D:/1.jpg";
		String content = "";
		try {
			FileInputStream fileForInput = new FileInputStream(picPath);
			byte[] bytes = new byte[fileForInput.available()];
			fileForInput.read(bytes);
			content = new sun.misc.BASE64Encoder().encode(bytes); // 具体的编码方法
			fileForInput.close();
			// System.out.println(content.length());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}

	public static void main(String[] args) throws Exception {
		URL url = new URL("http://192.168.45.17:8080/MiniWechat/TransferFile");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		// Read from the connection. Default is true.
		connection.setDoInput(true);
		// Set the post method. Default is GET
		connection.setRequestMethod("POST");
		// Post cannot use caches
		// Post 请求不能使用缓存
		connection.setUseCaches(false);
		// This method takes effects to
		// every instances of this class.
		// URLConnection.setFollowRedirects是static函数，作用于所有的URLConnection对象。
		// connection.setFollowRedirects(true);
		// This methods only
		// takes effacts to this
		// instance.
		// URLConnection.setInstanceFollowRedirects是成员函数，仅作用于当前函数
		connection.setInstanceFollowRedirects(false);
		// Set the content type to urlencoded,
		// because we will write
		// some URL-encoded content to the
		// connection. Settings above must be set before connect!
		// 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
		// 意思是正文是urlencoded编码过的form参数，下面我们可以看到我们对正文内容使用URLEncoder.encode
		// 进行编码
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		// 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
		// 要注意的是connection.getOutputStream会隐含的进行connect。
		connection.connect();
		Long sendTime = System.currentTimeMillis();
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		// 要传的参数
//		String content = URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("UpdateImage", "UTF-8");
//		content += "&" + URLEncoder.encode("imageName", "UTF-8") + "=" + URLEncoder.encode("1.jpg", "UTF-8");
		String content = "type=UpdateImage&imageName=1.jpg&";
		// 得到图片的base64编码
//		content = content + "&" + URLEncoder.encode("file", "UTF-8") + "=" + URLEncoder.encode(getPicBASE64(), "UTF-8");
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(content.getBytes());
//		System.out.println(ServletServer.CLIENT_PATH + "1.jpg");
		outputStream.write(DataTypeTranslater.fileToByte(ServletServer.CLIENT_PATH + "1.jpg"));
		
		out.write(outputStream.toByteArray());
		out.flush();
		out.close(); // flush and close
		// Get Session ID
		String key = "";
		if (connection != null) {
			for (int i = 1; (key = connection.getHeaderFieldKey(i)) != null; i++) {
				if (key.equalsIgnoreCase("set-cookie")) {
					sessionId = connection.getHeaderField(key);
					sessionId = sessionId.substring(0, sessionId.indexOf(";"));
				}
			}
		}
	}
}
