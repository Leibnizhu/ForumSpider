package leibniz.hu.forumspider;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Spider {
	private String initialURL;
	private String savepath;
	private ArrayList<String> keywords = new ArrayList<String>();
	private ArrayList<Map<String, String>> unHandleList = new ArrayList<Map<String, String>>();
	
	/**
	 * 从spider.cfg.xml文件中读取爬虫的配置
	 */
	private void readConfig(){
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			this.getClass().getClassLoader();
			Document document = db.parse(ClassLoader.getSystemResource("spider.cfg.xml").toString());
			
			//遍历所有节点
			NodeList allNodes = document.getChildNodes().item(0).getChildNodes();
			for(int j = 0; j < allNodes.getLength(); j++){
				Node tempNode = allNodes.item(j);
				String tempNodeName = tempNode.getNodeName();
				//找到初始url的节点，放入类的属性中
				if("starturl".equals(tempNodeName)){
					initialURL = tempNode.getTextContent();
				}
				//找到下载关键字的节点，将其中所有的关键字加入到类的属性中
				if("keywords".equals(tempNodeName)){
					NodeList keywordList = tempNode.getChildNodes();
					for (int i = 0; i < keywordList.getLength(); i++) {
		                Node keyword = keywordList.item(i);
		                if("keyword".equals(keyword.getNodeName())){
		                	keywords.add(keyword.getTextContent());
		                }
					}
				}
				//找到下载保存路径的节点，保存到对应属性中
				if("savepath".equals(tempNodeName)){
					savepath = tempNode.getTextContent();
				}
			}
			File saveDictionary = new File(savepath);
			if(!saveDictionary.exists()){
				saveDictionary.mkdirs();
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
				System.out.println("打开新一页帖子列表：" + curURL);
				
				//准备请求头部信息
				URLConnection conn = new URL(curURL).openConnection();
				conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
				conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				conn.setRequestProperty("Connection", "keep-alive");
				conn.setRequestProperty("Referer", initialURL);
				((HttpURLConnection) conn).setRequestMethod("GET");  
				
				//先直接读取整个页面
				StringBuffer bufHtml = new StringBuffer();
				Scanner scanner = new Scanner(conn.getInputStream());  
                while (scanner.hasNextLine()) {  
                	bufHtml.append(scanner.nextLine());  
                }
                String strHtml = bufHtml.toString();
                //到此读完整个页面，关闭资源
                scanner.close();
                ((HttpURLConnection)conn).disconnect();
                
                Matcher mArticleLink = pArticleLink.matcher(strHtml);
                //用while遍历整个网页所有的匹配的地址
                while(mArticleLink.find()){
                	//判断标题是否符合关键词
                	for(String keyword: keywords){
                		if(mArticleLink.group(2).contains(keyword)){
                			System.out.println("等待处理的帖子还有：" + unHandleList.size() + " 个");
                			//放入待处理队列
                			Map<String, String> tempResult = new HashMap<String, String>(); 
                			tempResult.put("title", mArticleLink.group(2));
                			tempResult.put("url", relativeURLHandler(mArticleLink.group(1)));
                			synchronized (unHandleList) {
                				unHandleList.add(tempResult);
                			}
                			//如果帖子分析器的线程不够，则开启种子线程
                			while(ArticleScanThread.threadNum <= 5) {
                				new Thread(new ArticleScanThread(unHandleList, savepath)).start();
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
                	break;
                }
			}
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
