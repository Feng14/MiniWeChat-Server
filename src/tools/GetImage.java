package tools;

import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import server.ResourcePath;

public class GetImage {
	static Logger logger = Logger.getLogger(GetImage.class);
	public static BufferedImage getImage(String imageName){
		logger.info("GetImage.getImage:begin to get default image:"+imageName);
		BufferedImage image = null;
		try{
			String urlStr=ResourcePath.getHeadDefaultPath()+imageName;
			logger.info("GetImage:imageUrl:"+urlStr);
			URL url = new URL(urlStr);  
			HttpURLConnection connection2 = (HttpURLConnection) url.openConnection();  
			String cookieVal=null;
			String key=null;
			String cookies=null;
			for (int i = 1; (key = connection2.getHeaderFieldKey(i)) != null; i++ ) {
		         if (key.equalsIgnoreCase("set-cookie")) {
		          cookieVal = connection2.getHeaderField(i);
		          cookieVal = cookieVal.substring(0, cookieVal.indexOf(";"));
		          cookies = cookies+cookieVal+";";
		         }
		      }
			connection2.connect();
			image = ImageIO.read(connection2.getInputStream()); 
			logger.info("GetImage.getImage:get default image:"+imageName+" success!");
			return image;
		}catch(Exception e){
			logger.error("GetImage.getImage:get default image:"+imageName+" fail!");
			logger.error(e.getStackTrace());
			return null;
		}
		
		
	}
	
	

}
