package server_HTTP;
public class ServletServer {
	
}
//
//import java.awt.image.BufferedImage;
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Iterator;
//import java.util.List;
//
//import javax.servlet.ServletContext;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.FileItemFactory;
//import org.apache.commons.fileupload.FileUploadException;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;
//import org.apache.log4j.PropertyConfigurator;
//
//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGImageDecoder;
//import com.sun.image.codec.jpeg.JPEGImageEncoder;
//
//import server.Server;
//import tools.DataTypeTranslater;
//import tools.Debug;
//
//public class ServletServer extends HttpServlet {
//	private static final String GIF = "image/gif;charset=GB2312";// 设定输出的类型
//	private static final String JPG = "image/jpeg;charset=GB2312";
//
//	public static final String SOURCE_PATH = "D:/Program/MiniWeChat-Server/Source";
//	public static final String CLIENT_PATH = SOURCE_PATH + "/FromClient/";
//	public static final String SERVER_PATH = SOURCE_PATH + "/FromServer/";
//
//	public Server minaServer;
//
//	@Override
//	public void init() {
//		System.out.println("I am be run!");
//		String path = getServletContext().getRealPath("/");
//		String configFile = path + getInitParameter("configFile");
//		PropertyConfigurator.configure(configFile);
////		try {
////			minaServer = new Server();
////		} catch (IOException e) {
////			Debug.log(Debug.LogType.FAULT, "ServletServer", "Start Mina Server Fail!\n" + e.toString());
////			System.err.println("ServletServer : 服务器启动失败");
////			e.printStackTrace();
////		}
//		// 初始化
////		if (Server.instance != null){
////			Server.instance.init();
////			Debug.log("ServletServer", "Initiate Mina Server Successful!");
////		}
////		else {
////			Debug.log(Debug.LogType.FAULT, "ServletServer", "Start Mina Server Fail!");
////		}
//	}
//
//	@Override
//	public void destroy() {
//		System.err.println("Oh Noooooooooooooooooo!!!!");
//		minaServer.onDestroy();
//	}
//
//	// 用于处理客户端发送的GET请求 　　
//	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		System.err.println("doGet");
//
//		// String type = request.getParameter("type");
//		// String imageName = request.getParameter("imageName");
//		// // request.getInputStream();
//		//
//		// System.out.println("type : " + type);
//		// System.out.println("imageName : " + imageName);
//		//
//		// if (type != null) {
//		// // 网客户端发图片
//		// if (type.equals("Image")) {
//		// String imagePath = CLIENT_PATH + imageName;
//		// sendImageStr(response, imagePath);
//		// System.out.println("发送图片完毕!");
//		// }
//		//
//		// // 从客户端接收图片
//		// if (type.equals("UpdateImage")) {
//		// // String imageStr = request.getParameter("file");
//		// // System.out.println("File : " + imageStr);
//		// // createImageFile(imageName, imageStr);
//		//
//		// createImageFile(SERVER_PATH + imageName, request);
//		// System.out.println("接收图片完毕!");
//		// }
//		// }
//
//		// 监测request中是否包含文件
//
//		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
//		if (isMultipart) {
//			System.out.println("包含文件");
//		} else {
//			System.out.println("不包含文件");
//		}
//
//		// Create a factory for disk-based file items
//		FileItemFactory factory = new DiskFileItemFactory();
//		// 获取文件需要上传到的路径
//		// String path = request.getRealPath("/upload");
//		String path = request.getSession().getServletContext().getRealPath("upload");
//
//		// 如果没以下两行设置的话，上传大的 文件 会占用 很多内存，
//		// 设置暂时存放的 存储室 , 这个存储室，可以和 最终存储文件 的目录不同
//		/**
//		 * 
//		 * 原理 它是先存到 暂时存储室，然后在真正写到 对应目录的硬盘上，
//		 * 
//		 * 按理来说 当上传一个文件时，其实是上传了两份，第一个是以 .tem 格式的
//		 * 
//		 * 然后再将其真正写到 对应目录的硬盘上
//		 */
//		// factory.setRepository(new File(path));
//
//		// 设置 缓存的大小，当上传文件的容量超过该缓存时，直接放到 暂时存储室
//		// factory.setSizeThreshold(1024*1024) ;
//
//		// Create a new file upload handler
//		ServletFileUpload upload = new ServletFileUpload(factory);
//
//		// Parse the request
//		List items = null;
//		try {
//			items = upload.parseRequest(request);
//		} catch (FileUploadException e) {
//			e.printStackTrace();
//		}
//
//		// Process the uploaded items
//		Iterator iter = items.iterator();
//
//		while (iter.hasNext()) {
//			FileItem item = (FileItem) iter.next();
//
//			if (item.isFormField()) {// 如果是普通表单控件
//				// //获取表单的属性名字
//				String name = item.getFieldName();
//				// 获取用户具体输入的字符串 ，名字起得挺好，因为表单提交过来的是 字符串类型的
//				String value = item.getString();
//
//				request.setAttribute(name, value);
//				System.out.println("name:" + name);
//				System.out.println("value:" + value);
//			} else {// 如果是文件
//				// 也叫name是为了和上面保持一致，方便显示页面
//				String name = item.getFieldName();
//				String fileName = item.getName();
//				String contentType = item.getContentType();
//				boolean isInMemory = item.isInMemory();
//				long sizeInBytes = item.getSize();
//
//				System.out.println("表单文件控件名:" + name);
//
//				// 绝对路径的
//				System.out.println("上传文件名:" + fileName);
//				System.out.println("文件类型:" + contentType);
//				System.out.println("是否保存在内存中:" + isInMemory);
//				System.out.println("大小:" + sizeInBytes);
//
//				// 上传文件
//				// 获取文件名
//				String f_name = fileName.substring(fileName.lastIndexOf("\\") + 1, fileName.length());
//				request.setAttribute(name, f_name);
//
//				// 进行文件上传
//				File uploadedFile = new File(path, f_name);
//				try {
//					item.write(uploadedFile);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//				/*
//				 * OutputStream out = new FileOutputStream(new
//				 * File(path,filename)); InputStream in = item.getInputStream();
//				 * 
//				 * int length = 0 ; byte [] buf = new byte[1024] ;
//				 * System.out.println("获取上传文件的总共的容量："+item.getSize());
//				 * 
//				 * // in.read(buf) 每次读到的数据存放在 buf 数组中 while( (length =
//				 * in.read(buf) ) != -1) { //在 buf 数组中 取出数据 写到 （输出流）磁盘上
//				 * out.write(buf, 0, length); } in.close();
//				 */
//
//			}
//
//		}
//
//	}
//
//	// 用于处理客户端发送的POST请求
//
//	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		doGet(request, response);// 这条语句的作用是，当客户端发送POST请求时，调用doGet()方法进行处理 　　
//	}
//
//	/**
//	 * 往客户端发图片
//	 * 
//	 * @author Feng
//	 */
//	public void sendImageStr(HttpServletResponse response, String imagePath) throws IOException {
//		OutputStream output = response.getOutputStream();// 得到输出流
//		if (imagePath.toLowerCase().endsWith(".jpg"))// 使用编码处理文件流的情况：
//		{
//			response.setContentType(JPG);// 设定输出的类型
//			// 得到图片的真实路径
//			// imagePath = getServletContext().getRealPath(imagePath);
//			// 得到图片的文件流
//			InputStream imageIn = new FileInputStream(new File(imagePath));
//			// 得到输入的编码器，将文件流进行jpg格式编码
//			JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(imageIn);
//			// 得到编码后的图片对象
//			BufferedImage image = decoder.decodeAsBufferedImage();
//			// 得到输出的编码器
//			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(output);
//			encoder.encode(image);// 对图片进行输出编码
//			imageIn.close();// 关闭文件流
//		}
//		if (imagePath.toLowerCase().endsWith(".gif"))// 不使用编码处理文件流的情况：
//		{
//			response.setContentType(GIF);
//			ServletContext context = getServletContext();// 得到背景对象
//			InputStream imageIn = context.getResourceAsStream(imagePath);// 文件流
//			BufferedInputStream bis = new BufferedInputStream(imageIn);// 输入缓冲流
//			BufferedOutputStream bos = new BufferedOutputStream(output);// 输出缓冲流
//			byte data[] = new byte[4096];// 缓冲字节数
//			int size = 0;
//			size = bis.read(data);
//			while (size != -1) {
//				bos.write(data, 0, size);
//				size = bis.read(data);
//			}
//			bis.close();
//			bos.flush();// 清空输出缓冲流
//			bos.close();
//		}
//		output.close();
//	}
//
//	/**
//	 * 往客户端发图片
//	 * 
//	 * @author Feng
//	 * @throws IOException
//	 */
//	public void sendImageBytes(HttpServletResponse response, String imagePath) throws IOException {
//		// FileInputStream inputStream = new FileInputStream(imagePath);
//		// ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1000);
//		// byte[] buffer = new byte[1000];
//		//
//		// int n ;
//		//
//		// while ((n = inputStream.read(buffer)) != -1)
//		// outputStream.write(buffer, 0, n);
//		//
//		// inputStream.close();
//		// outputStream.close();
//
//		// byte[] imageBytes = outputStream.toByteArray();
//		response.getOutputStream().write(DataTypeTranslater.fileToByte(imagePath));
//	}
//
//	/**
//	 * 转码并保存图片
//	 * 
//	 * @throws IOException
//	 * @author Feng
//	 */
//	public void createImageFile(String imageName, String imageStr) throws IOException {
//		byte[] bytes = new sun.misc.BASE64Decoder().decodeBuffer(imageStr);
//		FileOutputStream fileOutputStream = new FileOutputStream("D:/FromClient/" + imageName);
//		fileOutputStream.write(bytes);
//		fileOutputStream.close();
//
//		// byte[] bytes = new byte[fileForInput.available()];
//		// fileForInput.read(bytes);
//		// content = new sun.misc.BASE64Encoder().encode(bytes); // 具体的编码方法
//		// fileForInput.close();
//	}
//
//	/**
//	 * 保存来自客户端的图片
//	 * 
//	 * @throws IOException
//	 * @author Feng
//	 */
//	public void createImageFile(String address, HttpServletRequest request) throws IOException {
//		System.out.println(address);
//		File file = new File(address);
//		FileOutputStream outputStream = new FileOutputStream(file);
//
//		InputStream inputStream = request.getInputStream();
//
//		byte[] buffer = new byte[4096];
//		int readLength = -1;
//		while ((readLength = inputStream.read(buffer)) != -1) {
//			outputStream.write(buffer);
//		}
//		outputStream.flush();
//		outputStream.close();
//		inputStream.close();
//	}
//}
