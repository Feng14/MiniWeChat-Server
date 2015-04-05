package server_HTTP;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.PropertyConfigurator;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import server.Server;
import tools.DataTypeTranslater;

public class ServletServer extends HttpServlet {
	private static final String GIF = "image/gif;charset=GB2312";// 设定输出的类型
	private static final String JPG = "image/jpeg;charset=GB2312";
	
	public static final String SOURCE_PATH = "D:/Program/MiniWeChat-Server/Source";
	public static final String CLIENT_PATH = SOURCE_PATH + "/FromClient/";
	public static final String SERVER_PATH = SOURCE_PATH + "/FromServer/";
	
	public Server minaServer;

	@Override
	public void init() {
		System.out.println("I am be run!");
		String path = getServletContext().getRealPath("/");
    	String configFile = path + getInitParameter("configFile");
    	PropertyConfigurator.configure(configFile);
		try {
			minaServer = new Server();
		} catch (IOException e) {
			System.err.println("ServletServer : 服务器启动失败");
			e.printStackTrace();
		}
	}
	
	@Override
	public void destroy(){
		System.err.println("Oh Noooooooooooooooooo!!!!");
		minaServer.onDestroy();
	}

	// 用于处理客户端发送的GET请求 　　
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.err.println("doGet");

		String type = request.getParameter("type");
		String imageName = request.getParameter("imageName");
//		request.getInputStream();

		System.out.println("type : " + type);
		System.out.println("imageName : " + imageName);

		if (type != null) {
			// 网客户端发图片
			if (type.equals("Image")) {
				String imagePath = CLIENT_PATH + imageName;
				sendImageStr(response, imagePath);
				System.out.println("发送图片完毕!");
			}
			
			// 从客户端接收图片
			if (type.equals("UpdateImage")) {
//				String imageStr = request.getParameter("file");
//				System.out.println("File : " + imageStr);
//				createImageFile(imageName, imageStr);
				
				createImageFile(SERVER_PATH + imageName, request);
				System.out.println("接收图片完毕!");
			}
		}
	}

	// 用于处理客户端发送的POST请求

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);// 这条语句的作用是，当客户端发送POST请求时，调用doGet()方法进行处理 　　
	}

	/**
	 *  往客户端发图片
	 *  @author Feng
	 */
	public void sendImageStr(HttpServletResponse response, String imagePath) throws IOException {
		OutputStream output = response.getOutputStream();// 得到输出流
		if (imagePath.toLowerCase().endsWith(".jpg"))// 使用编码处理文件流的情况：
		{
			response.setContentType(JPG);// 设定输出的类型
			// 得到图片的真实路径
			// imagePath = getServletContext().getRealPath(imagePath);
			// 得到图片的文件流
			InputStream imageIn = new FileInputStream(new File(imagePath));
			// 得到输入的编码器，将文件流进行jpg格式编码
			JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(imageIn);
			// 得到编码后的图片对象
			BufferedImage image = decoder.decodeAsBufferedImage();
			// 得到输出的编码器
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(output);
			encoder.encode(image);// 对图片进行输出编码
			imageIn.close();// 关闭文件流
		}
		if (imagePath.toLowerCase().endsWith(".gif"))// 不使用编码处理文件流的情况：
		{
			response.setContentType(GIF);
			ServletContext context = getServletContext();// 得到背景对象
			InputStream imageIn = context.getResourceAsStream(imagePath);// 文件流
			BufferedInputStream bis = new BufferedInputStream(imageIn);// 输入缓冲流
			BufferedOutputStream bos = new BufferedOutputStream(output);// 输出缓冲流
			byte data[] = new byte[4096];// 缓冲字节数
			int size = 0;
			size = bis.read(data);
			while (size != -1) {
				bos.write(data, 0, size);
				size = bis.read(data);
			}
			bis.close();
			bos.flush();// 清空输出缓冲流
			bos.close();
		}
		output.close();
	}

	/**
	 * 往客户端发图片
	 * @author Feng
	 * @throws IOException 
	 */
	public void sendImageBytes(HttpServletResponse response, String imagePath) throws IOException {
//		FileInputStream inputStream = new FileInputStream(imagePath);
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1000);
//		byte[] buffer = new byte[1000];
//		
//		int n ;
//		
//		while ((n = inputStream.read(buffer)) != -1)
//			outputStream.write(buffer, 0, n);
//		
//		inputStream.close();
//		outputStream.close();
		
//		byte[] imageBytes = outputStream.toByteArray();
		response.getOutputStream().write(DataTypeTranslater.fileToByte(imagePath));
	}
	
	/**
	 * 转码并保存图片
	 * @throws IOException 
	 * @author Feng
	 */
	public void createImageFile(String imageName, String imageStr) throws IOException{
		byte[] bytes = new sun.misc.BASE64Decoder().decodeBuffer(imageStr);
		FileOutputStream fileOutputStream = new FileOutputStream("D:/FromClient/" + imageName);
		fileOutputStream.write(bytes);
		fileOutputStream.close();

//		byte[] bytes = new byte[fileForInput.available()];
//		fileForInput.read(bytes);
//		content = new sun.misc.BASE64Encoder().encode(bytes); // 具体的编码方法
//		fileForInput.close();
	}
	
	/**
	 * 保存来自客户端的图片
	 * @throws IOException 
	 * @author Feng
	 */
	public void createImageFile(String address, HttpServletRequest request) throws IOException {
		System.out.println(address);
		File file = new File(address);
		FileOutputStream outputStream = new FileOutputStream(file);
		
		InputStream inputStream = request.getInputStream();
		
		byte[] buffer = new byte[4096];
        int readLength = -1;
        while((readLength = inputStream.read(buffer)) != -1)
        {
        	outputStream.write(buffer);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
	}
}
