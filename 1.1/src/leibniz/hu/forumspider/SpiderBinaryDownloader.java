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
import java.util.Map;

public abstract class SpiderBinaryDownloader extends SpiderAntiCrawlerHandler{
	//下载的主体程序独立成方法，方便于重复尝试下载
	public void download(String imageURL, String saveDictionary, int tryCnt){
		if(tryCnt<=5){
			//e.g. http://2342.net/1/3.jpg
			String filename = imageURL.substring(imageURL.lastIndexOf('/') + 1);
			FileOutputStream fs = null;
			InputStream inStream = null;
			try {
				URLConnection conn = new URL(imageURL).openConnection();
				SpiderUtils.initReqHeader(conn, SpiderUtils.initialURL);
				inStream = conn.getInputStream(); 
				File fImg = new File(saveDictionary, filename);
				if(fImg.exists()){
					RandomAccessFile raf = new RandomAccessFile(fImg, "r");
					//通过判断文件结尾是否为0xff 0xd9来判定图片是否下载完整
					//不完整则重新下载，否则跳过return
					if(!isNeedReDownload(raf)){
						return;
					}
					raf.close();
				}
				fs = new FileOutputStream(fImg);  
				
				byte[] buffer = new byte[1024*64];  
				int byteread = 0;
				while ((byteread = inStream.read(buffer)) != -1) {  
					fs.write(buffer, 0, byteread);  
				}
				((HttpURLConnection)conn).disconnect();
			} catch (IOException e) {
				//先关闭资源再重新尝试，防止开启过多连接
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
				//再次尝试
				tryCnt++;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {}
				download((imageURL, saveDictionary, tryCnt);
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
		} else {
			System.out.println(new Date() + " 下载图片：" + imageURL + "失败！！");
		}
	}
	
	public boolean abstract isNeedReDownload(RandomAccessFile raf){};
}
