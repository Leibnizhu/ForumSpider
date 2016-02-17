package leibniz.hu.forumspider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import leibniz.hu.forumspider.SpiderMain;
import leibniz.hu.forumspider.SpiderUtils;
import leibniz.hu.forumspider.ThreadManager;

import org.junit.Test;

public class SpiderMain extends SpiderHtmlDownloader{
	private  ArrayList<Map<String, String>> unHandleList = new ArrayList<Map<String, String>>();
	private  ArrayList<Map<String, String>> imageDownList = new ArrayList<Map<String, String>>();
	//提供get函数供其他类查询队列
	public  ArrayList<Map<String, String>> getUnHandleList(){
		return unHandleList;
	}
	public ArrayList<Map<String, String>> getImageDownList(){
		return imageDownList;
	}
	
	//Singleton单例模式
	private static SpiderMain p = new SpiderMain();
	private SpiderMain(){}
	public static SpiderMain getSpiderInstance(){
		return p;
	}
	
	@Test
	//开启爬虫，读取每一页帖子列表，对其中满足关键字条件的帖子，将网址和标题放入任务队列
	public void startSpider(){
		try {
			String curURL = initialURL;
			String refURL = null;
			//判断下一页和帖子地址标题所需的正则表达式
			Pattern pArticleLink = Pattern.compile(articleInList);
			Pattern pNextLink = Pattern.compile(nextList);
						
			//开始遍历帖子
			while(true){
				System.out.println(new Date() + " 打开新一页帖子列表：" + curURL);

				//先直接读取整个页面
				String strHtml = downHtml(curURL, refURL, 0);
                System.out.println(new Date() + " 帖子列表" + curURL + "下载完毕，共计" + strHtml.length() + "字节。开始目标帖子地址……");
                
				if(null != strHtml && strHtml.length > minPageSize){
					Matcher mArticleLink = pArticleLink.matcher(strHtml);
					//用while遍历整个网页所有的匹配的地址
					while(mArticleLink.find()){
						//判断标题是否符合关键词
						for(String keyword: keywords){
							if(mArticleLink.group(2).contains(keyword)){
								//放入待处理队列
								Map<String, String> tempResult = new HashMap<String, String>(); 
								tempResult.put("title", mArticleLink.group(2));
								tempResult.put("url", SpiderUtils.relativeURLHandler(mArticleLink.group(1).replace("&amp;", "&")));
								unHandleList.add(tempResult);
								//跳出匹配关键词的循环
								break;
							}
						}//break跳出到这里，准备寻找下一个符合帖子链接正则的
					}
					//匹配下一页链接
					Matcher mNextLink = pNextLink.matcher(strHtml);
					if(mNextLink.find()){
						//记录来源页面
						refURL = curURL;
						curURL = SpiderUtils.relativeURLHandler(mNextLink.group(1).replace("&amp;", "&"));
					} else {
						//正确下载了页面而没有下一页链接
						break;
					}
				}
				//降低频率，防反爬，减轻其他线程负担
				Thread.sleep(5000);
			}
			System.out.println(new Date() + " 找不到下一页，主线程结束，当前帖子列表为：" +curURL);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		if(args.length != 0){
			readConfig(args[0]);
		} else{
			readConfig(null);
		}
		ThreadManager.managerGuard();
		SpiderMain.getSpiderInstance().startSpider();
	}
}
