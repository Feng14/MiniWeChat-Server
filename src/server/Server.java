package server;

import java.io.IOException;

/**
 * 服务器启动器
 * 
 * @author Feng
 * 
 */
public class Server {
	public static Server instance;
	private ServerNetwork serverNetwork;
	private ServerModel serverModel;
	private ServerModel_Chatting serverModel_Chatting;

//	public Server() throws IOException {
//		// 启动网络层
//		// ServerNetwork.instance.init();
//
//		// 启动逻辑层
//		// ServerModel.instance.init();
//	}
	
	/**
	 * 初始化
	 * @author Feng
	 */
	public void init(){
		try {
			serverNetwork.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverModel.init();
		serverModel_Chatting.init();
	}

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

	// public static void main(String[] args) throws IOException {
	// new Server();
	// }

}
