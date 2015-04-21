package server;

public class ResourcePath {
	/**
	 * 定义文件路径
	 */
	
	//头像路径
	private static String headPath_linux = "../miniwechatRes/head/";
	private static String headPath_windows ="d:\\miniwechatRes\\head\\";
	//默认头像路径
	private static String headDefaultPath_linux = "d:\\miniwechatRes\\headDefault\\";
	private static String headDefaultPath_windows = "../miniwechatRes/headDefault/";
	
	public static String getHeadPath(){
		if(System.getProperty("os.name").toLowerCase().indexOf("windows")>=0){
			return headPath_windows;
		}
		else{
			return headPath_linux;
		}
	}
	
	public static String getHeadDefaultPath(){
		if(System.getProperty("os.name").toLowerCase().indexOf("windows")>=0){
			return headDefaultPath_windows;
		}
		else{
			return headDefaultPath_linux;
		}
	}

}
