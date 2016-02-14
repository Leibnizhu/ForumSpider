package leibniz.hu.forumspider;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArticleScanThread implements Runnable {
	//帖子URL
	private String articleURL;
	//保存的文件夹路径（不包含帖子标题的子文件夹）
	private String originDictionary = SpiderUtils.savepath;
	//保存的文件夹路径（包含帖子标题的子文件夹）
	private String saveDictionary;
	private String nextPageRegax;
	private String imgAddrRegax;
	
	@Override
	public void run() {
		Map<String, String> tempMission = null;
		nextPageRegax = SpiderUtils.nextPage;
		imgAddrRegax = SpiderUtils.imgAddr;
		while(true){
			ThreadManager.managerGuard();
			
			if(Spider.getSpiderInstance().getUnHandleList().size() > 0){
				tempMission = Spider.getSpiderInstance().getUnHandleList().remove(0);
				//得到新任务的url及标题（保存路径）
				this.articleURL = tempMission.get("url");
				this.saveDictionary = originDictionary +"/" + tempMission.get("title");
				String curURL = articleURL;
				
				//图片链接的正则表达式
				//e.g. <img src="http://23423.net/7edaa21f1d5401.jpg" alt="" />
				Pattern pImageLink = Pattern.compile(imgAddrRegax);
				//下一页链接的正则表达式
				//e.g. 多页有下一页：href="/arthtml/4sdf1-2.html" class="pagelink_a">下一页</a>
				//e.g. 多页无下一页：href="/arthtml/64808.html">1</a>&nbsp;<a class="curr">2</a>&nbsp;<a class="nolink">下一页</a>
				//e.g. 单页无下一页：什么都没有
//				Pattern pNextLink = Pattern.compile("href=\"(/arthtml/.{1,20}?)\".{1,20}?>下一页</a>");
				Pattern pNextLink = Pattern.compile(nextPageRegax);
				//标识是否有下一页
				boolean nextFlag ;
				
				try {
					while(true){
						//System.out.println(new Date() + " 正在处理帖子《" + tempMission.get("title") + "》的新一页："  + curURL);
						nextFlag = false;
						
						//准备请求头部信息
						URLConnection conn = new URL(curURL).openConnection();
						SpiderUtils.initReqHeader(conn, curURL);
						((HttpURLConnection) conn).setRequestMethod("GET");
						
						//先直接读取整个页面
						StringBuffer bufHtml = new StringBuffer();
						Scanner scanner = new Scanner(conn.getInputStream());  
		                while (scanner.hasNextLine()) {  
		                	bufHtml.append(scanner.nextLine());  
		                }
		                String strHtml = bufHtml.toString();
		                //System.out.println(new Date() + " 帖子《" + tempMission.get("title") + "》下载完毕，共计" + strHtml.length() + "字节。开始解析图片地址……");
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
		                	//放入待处理队列
                			Map<String, String> tempResult = new HashMap<String, String>(); 
                			//System.out.println(SpiderUtils.relativeURLHandler(mImageLink.group(1)));
                			tempResult.put("imageDownURL", SpiderUtils.relativeURLHandler(mImageLink.group(1)));
                			tempResult.put("saveDictionary", saveDictionary);
                			Spider.getSpiderInstance().getImageDownList().add(tempResult);
		                }
		                
		                //匹配到下一页的链接
		                Matcher mNextLink = pNextLink.matcher(strHtml);
		                if(mNextLink.find()){
		                	curURL = SpiderUtils.relativeURLHandler(mNextLink.group(1).replace("&amp;", "&"));
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
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
		}
	}
}
