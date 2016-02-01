package leibniz.hu.forumspider;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArticleScanThread implements Runnable {
	//private ArrayList<Map<String, String>> unHandleList;
	//帖子URL
	private String articleURL;
	//保存的文件夹路径（不包含帖子标题的子文件夹）
	private String originDictionary = SpiderUtils.savepath;
	//保存的文件夹路径（包含帖子标题的子文件夹）
	private String saveDictionary;
	//统计帖子分析线程数
	//static int threadNum= 0;
	//当前线程的状态
	//public boolean stopable = false;
	
	@Override
	public void run() {
		Map<String, String> tempMission = null;
		/*ArticleScanThread.threadNum++;
		if(ArticleScanThread.threadNum%10 == 0){
			System.out.println(new Date() + " 解析器启动------------>当前运行的帖子解析器有" + ArticleScanThread.threadNum + "个");
		}*/
		while(true){
			//待办任务大于帖子解析器数量N倍则开启新线程
			/*if(Spider.unHandleList.size() / ArticleScanThread.threadNum >= 2){
				new Thread(new ArticleScanThread(), "articleScan-" + Math.random()).start();
			}*/
			//stopable = false;
			if(Spider.unHandleList.size() > 0){
				//synchronized (Spider.unHandleList) {
					//每次从待处理队列中取出一个任务
					tempMission = Spider.unHandleList.remove(0);
				//}
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
				
				try {
					while(true){
						System.out.println(new Date() + " 正在处理帖子《" + tempMission.get("title") + "》的新一页："  + curURL);
						nextFlag = false;
						
						//准备请求头部信息
						URLConnection conn = new URL(curURL).openConnection();
						SpiderUtils.initReqHeader(conn, articleURL);
						((HttpURLConnection) conn).setRequestMethod("GET");
						
						//先直接读取整个页面
						StringBuffer bufHtml = new StringBuffer();
						Scanner scanner = new Scanner(conn.getInputStream());  
		                while (scanner.hasNextLine()) {  
		                	bufHtml.append(scanner.nextLine());  
		                }
		                String strHtml = bufHtml.toString();
		                System.out.println(new Date() + " 帖子《" + tempMission.get("title") + "》下载完毕，共计" + strHtml.length() + "字节。开始解析图片地址……");
		                //new ThreadManager().ThreadController();
		                //读取完整个页面了，关闭资源
		                scanner.close();
		                ((HttpURLConnection)conn).disconnect();
		                
		                //匹配到图片链接
		                Matcher mImageLink = pImageLink.matcher(strHtml);
		                while(mImageLink.find()){
		                	//创建保存图片的子文件夹
		                	File saveDict = new File(saveDictionary);
		                	if(!saveDict.exists()){
		                		saveDict.mkdirs();
		                	}
		                	//new Thread(new ImageDownThread(mImageLink.group(1), saveDictionary)).start();
		                	//放入待处理队列
                			Map<String, String> tempResult = new HashMap<String, String>(); 
                			tempResult.put("imageDownURL", mImageLink.group(1));
                			tempResult.put("saveDictionary", saveDictionary);
                			//synchronized (Spider.imageDownList) {
                				Spider.imageDownList.add(tempResult);
                			//}
                			//如果照片下载器的线程不够，则开启种子线程
                			/*while(ImageDownThread.threadNum <= 20) {
                				new Thread(new ImageDownThread(), "imageDown-" + Math.random()).start();
                			}*/
                			/*if(Spider.imageDownList.size()%10 == 0){
                				System.out.println("等待下载的照片还有：" + Spider.imageDownList.size() + " 个" + ImageDownThread.threadNum);
                			}*/
		                }
		                
		                //匹配到下一页的链接
		                Matcher mNextLink = pNextLink.matcher(strHtml);
		                if(mNextLink.find()){
		                	curURL = sWebsiteLink + mNextLink.group(1);
		                	nextFlag = true;
		                }
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
			//stopable = true;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
			/*if(Spider.unHandleList.size() / ArticleScanThread.threadNum < 0.8 && ArticleScanThread.threadNum > 5){
				//待办任务小于帖子解析器数量M倍则关闭当前线程
				break;
			}*/
		}
		/*ArticleScanThread.threadNum--;
		if(ArticleScanThread.threadNum%10 == 0){
			System.out.println(new Date() + " 解析器关闭--------------------->当前运行的帖子解析器有" + ArticleScanThread.threadNum + "个");
		}*/
	}
}
