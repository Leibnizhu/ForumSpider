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
	//统计图片下载线程数
	static int threadNum= 0;
	
	/*
	public ImageDownThread(String imageURL, String saveDictionary) {
		super();
		this.imageURL = imageURL;
		this.saveDictionary = saveDictionary;
	}*/

	@Override
	public void run() {
		threadNum++;
		Map<String, String> tempMission = null;
		if(threadNum%10 == 0){
			System.out.println(new Date() + " 图片下载启动------------------>像我这么屌的图片下载器有" + threadNum + "个");
		}
		while(true){
			//待办任务大于帖子解析器数量N倍则开启新线程
			if(Spider.imageDownList.size() / threadNum >= 1.5){
				new Thread(new ImageDownThread(), "imageDown-" + Math.random()).start();
			}
			if(Spider.imageDownList.size() > 0){
				//synchronized (Spider.imageDownList) {
					//每次从待处理队列中取出一个任务
					tempMission = Spider.imageDownList.remove(0);
				//}
				//得到新任务的url及标题（保存路径）
				this.imageURL = tempMission.get("imageDownURL");
				this.saveDictionary = tempMission.get("saveDictionary");
				
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
					threadNum--;
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
			try {
				Thread.sleep(50000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(Spider.imageDownList.size() / threadNum < 0.6 && threadNum > 20){
				//待办任务小于帖子解析器数量M倍则关闭当前线程
				break;
			}
		}
		threadNum--;
		if(threadNum%10 == 0){
			System.out.println(new Date() + " 图片下载器关闭-------------------------->当前运行的图片下载器有" + threadNum + "个");
		}
	}
}
