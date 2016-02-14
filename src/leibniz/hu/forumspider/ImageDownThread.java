package leibniz.hu.forumspider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Map;

public class ImageDownThread implements Runnable {
	private String imageURL;
	private String saveDictionary;
	private int tryCnt;
	
	@Override
	public void run() {
		Map<String, String> tempMission = null;
		while(true){
			if(Spider.getSpiderInstance().getImageDownList().size() > 0){
				//每次从待处理队列中取出一个任务
				tempMission = Spider.getSpiderInstance().getImageDownList().remove(0);
				//得到新任务的url及标题（保存路径）
				this.imageURL = tempMission.get("imageDownURL");
				this.saveDictionary = tempMission.get("saveDictionary");
				tryCnt = 0;
				download();			
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public void download(){
		if(tryCnt<=5){
			//e.g. http://2342.net/1/3.jpg
			String filename = imageURL.substring(imageURL.lastIndexOf('/') + 1);
			FileOutputStream fs = null;
			InputStream inStream = null;
			try {
				URLConnection conn = new URL(imageURL).openConnection();
				SpiderUtils.initReqHeader(conn, SpiderUtils.initialURL);
				conn.setConnectTimeout(300*1000);
				inStream = conn.getInputStream(); 
				File fImg = new File(saveDictionary, filename);
				if(fImg.exists()){
					//return;
				}
				fs = new FileOutputStream(fImg);  
				
				byte[] buffer = new byte[1024*64];  
				int byteread = 0;
				while ((byteread = inStream.read(buffer)) != -1) {  
					fs.write(buffer, 0, byteread);  
				}
				((HttpURLConnection)conn).disconnect();
			} catch (IOException e) {
				//e.printStackTrace();
				System.out.println(new Date() + " 下载图片：" + imageURL + "失败！！");
				tryCnt++;
				try {
					if(fs != null){
						fs.close();
					}
					if(inStream != null){
						inStream.close();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				download();
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
}
