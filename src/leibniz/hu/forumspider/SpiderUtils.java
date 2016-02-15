package leibniz.hu.forumspider;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SpiderUtils {
	public static String initialURL = null;
	public static String savepath = null;
	public static String nextPage = null;
	public static String nextList = null;
	public static String articleInList = null;
	public static String imgAddr = null;
	public static ArrayList<String> keywords = new ArrayList<String>();
	private static Map<String,String> cookieMap = new HashMap<String, String>();
	
	public static void initReqHeader(URLConnection conn, String refURL){
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36");
		conn.setRequestProperty("Cache-control","no-cache, no-store");
		conn.setRequestProperty("DNT", "1");
		if(null != refURL){
			conn.setRequestProperty("Referer", refURL);
		}
		conn.setRequestProperty("Accept-Encoding", "deflate, sdch");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
		Set<String> cookieKeys = cookieMap.keySet();
		for(String key:cookieKeys){
			conn.addRequestProperty("cookie", key + "=" + cookieMap.get(key));
		}
		conn.setConnectTimeout(30000);  
	    conn.setReadTimeout(30000);
	}
	
	//处理相对URl路径，获取绝对URL路径
	public static String relativeURLHandler(String relativeURL){
		String rootURL =  initialURL.substring(0, initialURL.indexOf("/", 7));
		String curParentURL  = initialURL.substring(0, initialURL.lastIndexOf("/"));
		if(relativeURL.startsWith("/")){
			//相对于网站根目录的地址
			return rootURL + relativeURL;
		} if(relativeURL.startsWith("http://")){
			return relativeURL;
		}else {
			return curParentURL + "/" + relativeURL;
		}
	}
	
	//从connection的响应中获取cookie设定信息，更新原有cookieMap
	public static void getCookie(URLConnection conn){
		List<String> listTemp= conn.getHeaderFields().get("Set-Cookie");
		for(String cookie : listTemp){
			String strTemp = cookie.split(";")[0];
			//因为是Map,旧的Cookie会被替代
			cookieMap.put(strTemp.split("=")[0], strTemp.split("=")[1]);
		}
		//参照chrome发出的cookies，两个可有可无的cookies
		cookieMap.put("AJSTAT_ok_times", "1");
		if(null == cookieMap.get("AJSTAT_ok_pages")){
			cookieMap.put("AJSTAT_ok_pages", "1");
		} else {
			cookieMap.put("AJSTAT_ok_pages", String.valueOf((Integer.parseInt(cookieMap.get("AJSTAT_ok_pages")) + 1)));
		}
	}
	
	//将下载网页独立成方法，便于多次尝试下载
	//tryCnt为尝试的次数，该方法返回读取到的StringBuffer
	private String downHtml(URLConnection conn, int tryCnt){
		Scanner scanner = null;
		StringBuffer bufHtml = new StringBuffer();
		try{
			scanner = new Scanner(conn.getInputStream());  
			while (scanner.hasNextLine()) {  
				bufHtml.append(scanner.nextLine());  
			}
			if(tryCnt>0){
				System.out.println(bufHtml);
			}
			return bufHtml.toString();
		} catch (IOException e) {
			tryCnt++;
			if(tryCnt < 5){
				//尝试5次
				return downHtml(conn, tryCnt);
			} else {
				return null;
			}
		} finally {
			try{
				//到此读完整个页面，关闭资源
				if(null != scanner){
					scanner.close();
				}
			} catch (IOException e) {
					e.printStackTrace();
			}
		}
	}
	
	//从spider.cfg.xml中读取配置
	public static void readConfig(){
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
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
					//去掉尾部\或/
					if(savepath.endsWith("/") || savepath.endsWith("\\")){
						savepath = savepath.substring(0, savepath.length() -1);
					}
				}
				//找到下一页帖子的正则表达式，放入属性
				if("next-page".equals(tempNodeName)){
					nextPage = tempNode.getTextContent();
				}
				//找到下一个帖子列表的正则表达式，放入属性
				if("next-list".equals(tempNodeName)){
					nextList = tempNode.getTextContent();
				}
				//找到列表中帖子的正则表达式，放入属性
				if("article-in-list".equals(tempNodeName)){
					articleInList = tempNode.getTextContent();
				}
				//找到帖子中图片地址的正则表达式，放入属性
				if("image-addr".equals(tempNodeName)){
					imgAddr = tempNode.getTextContent();
				}
			}
			createSaveDict();
			System.out.println(nextPage + "\n" + nextList + "\n" + articleInList);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void createSaveDict(){
		if(null != savepath){
			File saveDictionary = new File(savepath);
			if(!saveDictionary.exists()){
				saveDictionary.mkdirs();
			}
		}
	}
}
