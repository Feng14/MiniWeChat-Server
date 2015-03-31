package server;

import java.io.IOException;

/**
 * 服务器启动器
 * @author Feng
 *
 */
public class Server {
	
	public Server() throws IOException{
		// 启动网络层
		ServerNetwork.instance.init();
		
		// 启动逻辑层
		ServerModel.instance.init();
	}
	
	public static void main(String[] args) throws IOException {
		new Server();
	}

}
