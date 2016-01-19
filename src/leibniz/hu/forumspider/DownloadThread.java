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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DownloadThread implements Runnable {
	//帖子URL
	private String articleURL;
	//保存的文件夹路径（已包含帖子标题的子文件夹）
	private String dictionary;
	
	public DownloadThread(ArrayList<Map<String, String>> unHandleList, String dictionary) {
		super();
		Map<String, String> tempResult = unHandleList.remove(0);
		this.articleURL = tempResult.get("url");
		this.dictionary = dictionary + tempResult.get("title");
	}
	
	private void downImage(String imageURL){
		//e.g. http://2342.net/1/323544ufc2de51aa6204.jpg
		String filename = imageURL.substring(imageURL.lastIndexOf('/') + 1);
		System.out.println("正在下载图片: " + imageURL);
		
		try {
			URLConnection conn = new URL(imageURL).openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
			InputStream inStream = conn.getInputStream();  
			FileOutputStream fs = new FileOutputStream(new File(dictionary, filename));  
			
			byte[] buffer = new byte[1204];  
			int byteread = 0;
			while ((byteread = inStream.read(buffer)) != -1) {  
				fs.write(buffer, 0, byteread);  
			} 
			fs.close();
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
		//标识是否有下一页
		boolean nextFlag ;
		
		//创建保存图片的子文件夹
		File saveDictionary = new File(dictionary);
		if(!saveDictionary.exists()){
			saveDictionary.mkdirs();
		}
		try {
			while(true){
				nextFlag = false;
				URLConnection conn = new URL(curURL).openConnection();
				conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
				BufferedReader brWeb = new BufferedReader(new InputStreamReader(conn.getInputStream()), 1024*120);
				String line = null;
				//逐行读取返回的页面
				while((line = brWeb.readLine()) != null) {
					//匹配到图片链接
					Matcher mImageLink = pImageLink.matcher(line);
					while(mImageLink.find()){
						//System.out.println("image: " +mImageLink.group(1));
						for(int i = 1; i <= mImageLink.groupCount(); i++){
							downImage(mImageLink.group(i));
						}
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
