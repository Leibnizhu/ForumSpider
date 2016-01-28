package leibniz.hu.forumspider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

public class ImageDownThread implements Runnable {
	private String imageURL;
	private String saveDictionary;
	
	public ImageDownThread(String imageURL, String saveDictionary) {
		super();
		this.imageURL = imageURL;
		this.saveDictionary = saveDictionary;
	}

	@Override
	public void run() {
		//e.g. http://2342.net/1/323544ufc2de51aa6204.jpg
		String filename = imageURL.substring(imageURL.lastIndexOf('/') + 1);
		FileOutputStream fs = null;
		InputStream inStream = null;
		
		try {
			URLConnection conn = new URL(imageURL).openConnection();
			SpiderUtils.initReqHeader(conn, "http://www.dedeni.com/artlist/7.html");
			conn.setConnectTimeout(300*1000);
			inStream = conn.getInputStream(); 
			File fImg = new File(saveDictionary, filename);
			if(fImg.exists()){
				return;
			}
			fs = new FileOutputStream(fImg);  
			
			byte[] buffer = new byte[1024*64];  
			int byteread = 0;
			while ((byteread = inStream.read(buffer)) != -1) {  
				fs.write(buffer, 0, byteread);  
			}
			((HttpURLConnection)conn).disconnect();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(new Date() + " 下载图片：" + imageURL + "失败！！");
		}  finally{
			try {
				if(fs != null){
					fs.close();
				}
				if(inStream != null){
					inStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
