package leibniz.hu.forumspider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DownloadThread implements Runnable {

	private String articleURL;
	
	public DownloadThread(ArrayList<String> unHandleList) {
		super();
		this.articleURL = unHandleList.remove(0);
	}
	
	private void downImage(String imageURL){
		//e.g. http://2342.net/1/323544ufc2de51aa6204.jpg
		String filename = imageURL.substring(imageURL.lastIndexOf('/') + 1);
		
		try {
			URLConnection conn = new URL(imageURL).openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
			InputStream inStream = conn.getInputStream();  
			FileOutputStream fs = new FileOutputStream(new File("down", filename));  
			
			byte[] buffer = new byte[1204];  
			int byteread = 0;
			while ((byteread = inStream.read(buffer)) != -1) {  
				fs.write(buffer, 0, byteread);  
			}  
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}

	@Override
	public void run() {
		//System.out.println(Thread.currentThread() + "," + articleURL);
		String curURL = articleURL;
		String sTemp = articleURL.substring(0, articleURL.lastIndexOf('/'));
		String sWebsiteLink = sTemp.substring(0, sTemp.lastIndexOf('/'));
		//图片链接的正则表达式
		//e.g. <img src="http://23423.net/7edaa21f1d5401.jpg" alt="" />
		Pattern pImageLink = Pattern.compile("<img src=\"(.+?jpg)\".+?/>");
		//下一页链接的正则表达式
		//e.g.  href="/arthtml/4sdf1-2.html" class="pagelink_a">下一页</a>
		Pattern pNextLink = Pattern.compile("href=\"(/arthtml/.{1,20}?)\".{1,30}?>下一页</a>");
		//没有下一页的正则表达式
		//e.g. <a class="nolink">下一页</a>
		//Pattern pNonextLink = Pattern.compile("<a class=\"nolink\">下一页</a>");
		boolean nextFlag ;
		
		try {
			while(true){
				nextFlag = false;
				URLConnection conn = new URL(curURL).openConnection();
				conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
				BufferedReader brWeb = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line = null;
				//逐行读取返回的页面
				while((line = brWeb.readLine()) != null) {
					//匹配到图片链接
					Matcher mImageLink = pImageLink.matcher(line);
					while(mImageLink.find()){
						//System.out.println("image: " +mImageLink.group(1));
						downImage(mImageLink.group(1));
					}
					//匹配到下一页的链接
					Matcher mNextLink = pNextLink.matcher(line);
					while(mNextLink.find()){
						System.out.println(mNextLink.group(1));
						curURL = sWebsiteLink + mNextLink.group(1);
						nextFlag = true;
					}
				}
				//读取完整个页面了
				//还是没找到下一页的话，退出循环
				if(nextFlag == false){
					break;
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
