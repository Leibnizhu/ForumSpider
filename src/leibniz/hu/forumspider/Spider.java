package leibniz.hu.forumspider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class Spider {
	private String initialURL;
	private ArrayList<String> keywords = new ArrayList<String>();
	static ArrayList<Map<String, String>> unHandleList = new ArrayList<Map<String, String>>();
	static ArrayList<Map<String, String>> imageDownList = new ArrayList<Map<String, String>>();
	
	/**
	 * 从spider.cfg.xml文件中读取爬虫的配置
	 */
	//@SuppressWarnings("unchecked")
	private void readConfig(){
		SpiderUtils.readConfig();
		initialURL =SpiderUtils.initialURL;
		keywords = SpiderUtils.keywords;
		
	}
	
	private String relativeURLHandler(String relativeURL){
		String rootURL =  initialURL.substring(0, initialURL.indexOf("/", 7));
		String curParentURL  = initialURL.substring(0, initialURL.lastIndexOf("/"));
		if(relativeURL.startsWith("/")){
			//相对于网站根目录的地址
			return rootURL + relativeURL;
		} else {
			return curParentURL + relativeURL;
		}
	}
	
	@Test
	public void startSpider(){
		readConfig();
		try {
			String curURL = initialURL;
			//Regax for hyper links.
			//e.g. <a href="/arthtml/233.html" target="_blank">收到分公司到法规水电费过</a>
			Pattern pArticleLink = Pattern.compile("<a\\s*href=\"(/arthtml/.+?)\".+?>(.+?)</a>");
			// e.g. href="/artlist/7-233.html" class="pagelink_a">下一页</a>
			Pattern pNextLink = Pattern.compile("href=\"(/artlist/.{1,20}?)\".{1,30}?>下一页</a>");
			//开始遍历帖子
			while(true){
				System.out.println(new Date() + " 打开新一页帖子列表：" + curURL);
				
				//准备请求头部信息
				URLConnection conn = new URL(curURL).openConnection();
				SpiderUtils.initReqHeader(conn, initialURL);
				((HttpURLConnection) conn).setRequestMethod("GET");  
				
				//先直接读取整个页面
				StringBuffer bufHtml = new StringBuffer();
				Scanner scanner = new Scanner(conn.getInputStream());  
                while (scanner.hasNextLine()) {  
                	bufHtml.append(scanner.nextLine());  
                }
                String strHtml = bufHtml.toString();
                System.out.println(new Date() + " 帖子列表" + curURL + "下载完毕，共计" + strHtml.length() + "字节。开始目标帖子地址……");
                //到此读完整个页面，关闭资源
                scanner.close();
                ((HttpURLConnection)conn).disconnect();
                
                Matcher mArticleLink = pArticleLink.matcher(strHtml);
                //用while遍历整个网页所有的匹配的地址
                while(mArticleLink.find()){
                	//判断标题是否符合关键词
                	for(String keyword: keywords){
                		if(mArticleLink.group(2).contains(keyword)){
                			//放入待处理队列
                			Map<String, String> tempResult = new HashMap<String, String>(); 
                			tempResult.put("title", mArticleLink.group(2));
                			tempResult.put("url", relativeURLHandler(mArticleLink.group(1)));
                			synchronized (unHandleList) {
                				unHandleList.add(tempResult);
                			}
                			//如果帖子分析器的线程不够，则开启种子线程
                			while(ArticleScanThread.threadNum <= 5) {
                				new Thread(new ArticleScanThread(), "articleScan-"+ Math.random()).start();
                			}
                			if(unHandleList.size()%10 == 0){
                				System.out.println("等待处理的帖子还有：" + unHandleList.size() + " 个");
                			}
                			//跳出匹配关键词的循环
                			break;
                		}
                	}//break跳出到这里，准备寻找下一个符合帖子链接正则的
                }
                //匹配下一页链接
                Matcher mNextLink = pNextLink.matcher(strHtml);
                if(mNextLink.find()){
                	curURL = relativeURLHandler(mNextLink.group(1));
                } else {
                	break;
                }
			}
			System.out.println(new Date() + " 找不到下一页，主线程结束，当前帖子列表为：" +curURL);
			while(true){}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		new Spider().startSpider();
	}
}