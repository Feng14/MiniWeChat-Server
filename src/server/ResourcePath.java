package server;

public class ResourcePath {
	/**
	 * 定义文件路径
	 */
	
	//头像路径
	private static String headPath_linux = "/usr/local/apache-tomcat-7.0.59/webapps/miniwechatRes/head/";
	private static String headPath_windows ="d:\\miniwechatRes\\head\\";
	//默认头像路径
	private static String headDefaultPath_all = "http://127.0.0.1:8080/miniwechatRes/headDefault/";
	
	public static String getHeadPath(){
		if(System.getProperty("os.name").toLowerCase().indexOf("windows")>=0){
			return headPath_windows;
		}
		else{
			return headPath_linux;
		}
	}
	
	public static String getHeadDefaultPath(){
		return headDefaultPath_all;
	}

}
