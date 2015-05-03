package server;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;

import model.HibernateSessionFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Session;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tools.AutoResponseClient;

/**
 * 服务器启动器
 * 
 * @author Feng
 * 
 */
public class Server {
	public static boolean keepAliveSwitch = true;
//	public static boolean keepAliveSwitch = false;
	static Logger logger = Logger.getLogger(Server.class);
	
	public static Server instance;
	private ServerNetwork serverNetwork;
	private ServerModel serverModel;
	private ServerModel_Chatting serverModel_Chatting;

	public Server() throws IOException {
//		// 启动网络层
//		// ServerNetwork.instance.init();
//
//		// 启动逻辑层
//		// ServerModel.instance.init();
//		init();
	}
	
	/**
	 * 初始化
	 * @author Feng
	 */
//	public void init(){
//		try {
//			serverNetwork.init();
//			
//			// 开启自动回复器
//			new AutoResponseClient();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		serverModel.init();
//		serverModel_Chatting.init();
//	}

	public static Server getServer() {
		return instance;
	}

	public static void setServer(Server server) {
		Server.instance = server;
	}

	public ServerNetwork getServerNetwork() {
		return serverNetwork;
	}

	public void setServerNetwork(ServerNetwork serverNetwork) {
		this.serverNetwork = serverNetwork;
	}

	public ServerModel getServerModel() {
		return serverModel;
	}

	public void setServerModel(ServerModel serverModel) {
		this.serverModel = serverModel;
	}
	

	public ServerModel_Chatting getServerModel_Chatting() {
		return serverModel_Chatting;
	}

	public void setServerModel_Chatting(ServerModel_Chatting serverModel_Chatting) {
		this.serverModel_Chatting = serverModel_Chatting;
	}

	public void onDestroy() {
		serverNetwork.onDestroy();
	}

	 public static void main(String[] args) throws IOException {
		 String path = System.getProperty("user.dir");
		 logger.info(path);
		 try{
		 	 String logConfigPath = "Log4JConfig.properties";
		 	 logger.info(logConfigPath);
		 	 PropertyConfigurator.configure(logConfigPath);
		 	 logger.info("log configure load success");	
		 }catch(Exception e){
			 logger.info("log configure load fail");
		 	 e.printStackTrace();
		 }
		 try{
			 Session session = HibernateSessionFactory.getSession();
			 session.close();
			 logger.info("数据库配置加载成功");
		 }catch(Exception e){
			 logger.error("数据库配置加载失败");
		 }
//		 try{
//			 String springConfigPath = "src/applicationContext.xml";
//			 PropertyConfigurator.configure(springConfigPath);
//			 logger.info("spring configure load success");	
//		 }catch(Exception e){
//			 System.out.println("spring configure load fail");
//			 e.printStackTrace();
//		 }
		 
//		 File file = new File("src/applicationContext.xml");
//		 System.out.println(file.exists());
		 ApplicationContext ctx = new FileSystemXmlApplicationContext("applicationContext.xml");
		 ((ServerNetwork) ctx.getBean("ServerNetwork")).init();
		 ((ServerModel)ctx.getBean("ServerModel")).init();
		 ((MyLogger)ctx.getBean("MyLogger")).closeLoggerNotWant();
		 
		 new AutoResponseClient();
//		 ServerNetwork serverNetwork = (ServerNetwork)ctx.getBean("ServerNetwork");
	 }
}
