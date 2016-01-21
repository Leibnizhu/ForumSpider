package leibniz.hu.forumspider;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArticleScanThread implements Runnable {
	private ArrayList<Map<String, String>> unHandleList;
	//帖子URL
	private String articleURL;
	//保存的文件夹路径（不包含帖子标题的子文件夹）
	private String originDictionary;
	//保存的文件夹路径（包含帖子标题的子文件夹）
	private String saveDictionary;
	//统计帖子分析线程数
	public static int threadNum= 0;
	
	public ArticleScanThread(ArrayList<Map<String, String>> unHandleList, String dictionary) {
		super();
		this.originDictionary = dictionary;
		this.unHandleList = unHandleList;
	}
	
	@Override
	public void run() {
		threadNum++;
		System.out.println("解析器启动------------>当前运行的帖子解析器有" + threadNum + "个");
		while(true){
			if(unHandleList.size() / threadNum >= 2){
				//待办任务大于帖子解析器数量N倍则开启新线程
				new Thread(new ArticleScanThread(unHandleList, originDictionary)).start();
			}
			Map<String, String> tempMission = null;
			if(unHandleList.size() > 0){
				synchronized (unHandleList) {
					//每次从待处理队列中取出一个任务
					tempMission = unHandleList.remove(0);
				}
				//得到新任务的url及标题（保存路径）
				this.articleURL = tempMission.get("url");
				this.saveDictionary = originDictionary +"/" + tempMission.get("title");
				String curURL = articleURL;
				String sTemp = articleURL.substring(0, articleURL.lastIndexOf('/'));
				String sWebsiteLink = sTemp.substring(0, sTemp.lastIndexOf('/'));
				
				//图片链接的正则表达式
				//e.g. <img src="http://23423.net/7edaa21f1d5401.jpg" alt="" />
				Pattern pImageLink = Pattern.compile("<img src=\"(.+?jpg)\".+?/>");
				//下一页链接的正则表达式
				//e.g. 多页有下一页：href="/arthtml/4sdf1-2.html" class="pagelink_a">下一页</a>
				//e.g. 多页无下一页：href="/arthtml/64808.html">1</a>&nbsp;<a class="curr">2</a>&nbsp;<a class="nolink">下一页</a>
				//e.g. 单页无下一页：什么都没有
				Pattern pNextLink = Pattern.compile("href=\"(/arthtml/.{1,20}?)\".{1,20}?>下一页</a>");
				//标识是否有下一页
				boolean nextFlag ;
				
				
				InputStreamReader brWeb  = null;
				try {
					while(true){
						System.out.println("正在处理帖子《" + tempMission.get("title") + "》的新一页："  + curURL);
						nextFlag = false;
						URLConnection conn = new URL(curURL).openConnection();
						conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
						conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
						conn.setRequestProperty("Connection", "keep-alive");
						conn.setRequestProperty("Referer", articleURL);
						((HttpURLConnection) conn).setRequestMethod("GET");  
						brWeb = new InputStreamReader(conn.getInputStream());
						char[] cbuf = new char[1024*100];
						String line = null;
						//逐行读取返回的页面
						int i;
						while((i=brWeb.read(cbuf)) > 0) {
							System.out.print(i+",");
							line = String.valueOf(cbuf);
							//匹配到图片链接
							Matcher mImageLink = pImageLink.matcher(line);
							while(mImageLink.find()){
									//Thread.sleep(2000);
									//创建保存图片的子文件夹
									File saveDict = new File(saveDictionary);
									if(!saveDict.exists()){
										saveDict.mkdirs();
									}
									new Thread(new ImageDownThread(mImageLink.group(1), saveDictionary)).start();
								//}
							}
							//匹配到下一页的链接
							Matcher mNextLink = pNextLink.matcher(line);
							if(mNextLink.find()){
								curURL = sWebsiteLink + mNextLink.group(1);
								nextFlag = true;
							}
						}
						//读取完整个页面了，关闭资源
						if(brWeb != null){
							brWeb.close();
						}
						((HttpURLConnection)conn).disconnect();
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
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(unHandleList.size() / threadNum < 0.8 && threadNum > 5){
				//待办任务小于帖子解析器数量M倍则关闭当前线程
				break;
			}
		}
		threadNum--;
		System.out.println("解析器关闭--------------------->当前运行的帖子解析器有" + threadNum + "个");
	}
}
