package leibniz.hu.forumspider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

public abstract class SpiderBinaryDownloader extends SpiderAntiCrawlerHandler{
	//由子类实现的抽象方法，判断当前的重名文件是否需要重新下载
	public abstract boolean isNeedReDownload(File fImg);
	
	//下载的主体程序独立成方法，方便于重复尝试下载
	public boolean downImage(String imageURL, String saveDictionary, int tryCnt){
		String filename = imageURL.substring(imageURL.lastIndexOf('/') + 1);
		FileOutputStream fs = null;
		InputStream inStream = null;
		URLConnection conn = null;
		try {
			conn = new URL(imageURL).openConnection();
			initReqHeader(conn, initialURL);
			inStream = conn.getInputStream(); 
			File fImg = new File(saveDictionary, filename);
			if(fImg.exists()){
				if(!isNeedReDownload(fImg)){
					return false;
				}
			}
			
			fs = new FileOutputStream(fImg);  
			byte[] buffer = new byte[1024*64];  
			int byteread = 0;
			while ((byteread = inStream.read(buffer)) != -1) {  
				fs.write(buffer, 0, byteread);  
			}
			
			return true;
		} catch (IOException e) {
			//先关闭资源再重新尝试，防止开启过多连接
			try {
				if(null != fs){
					fs.close();
				}
				if(null != inStream){
					inStream.close();
				}
				if(null != conn){
					((HttpURLConnection)conn).disconnect();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//再次尝试
			tryCnt++;
			if(tryCnt < 8){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					//由ThreadManager发出的中断，无视
				}
				return downImage(imageURL, saveDictionary, tryCnt);
			} else {
				System.out.println(new Date() + " 下载图片：" + imageURL + "失败！！");
				return false;
			}
		}  finally{
			try {
				if(null != fs){
					fs.close();
				}
				if(null != inStream){
					inStream.close();
				}
				if(null != conn){
					((HttpURLConnection)conn).disconnect();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
