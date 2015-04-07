package tools;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;
import server.ResourcePath;

public class GetImage {
	public static BufferedImage getImage(String imageName){
		BufferedImage image = null;
		try{
			String urlStr=ResourcePath.getHeadDefaultPath()+imageName;
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
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return image;
	}
	
	

}
