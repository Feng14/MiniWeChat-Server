package tools;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletServer extends HttpServlet {

	// 用于处理客户端发送的GET请求 　　
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.err.println("doGet");
//		response.setContentType("text/html;charset=UTF-8");// 这条语句指明了向客户端发送的内容格式和采用的字符编码．
//		PrintWriter out = response.getWriter();
//		out.println(" 您好！");// 利用PrintWriter对象的方法将数据发送给客户端 　　
//		out.close();
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("＜html＞＜head＞＜title＞");
		out.println("This is my first Servlet");
		out.println("＜/title＞＜/head＞＜body＞");
		out.println("＜h1＞Hello,World!＜/h1＞");
		out.println("＜/body＞＜/html＞");
	}

	// 用于处理客户端发送的POST请求

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);// 这条语句的作用是，当客户端发送POST请求时，调用doGet()方法进行处理 　　
	}
}
