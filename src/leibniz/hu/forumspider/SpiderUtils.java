package leibniz.hu.forumspider;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;

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
	public static ArrayList<String> keywords = new ArrayList<String>();
	
	public static void initReqHeader(URLConnection conn, String refURL){
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Referer", refURL);
	}
	
	//处理相对URl路径，获取绝对URL路径
	private static String relativeURLHandler(String initialURL, String relativeURL){
		String rootURL =  initialURL.substring(0, initialURL.indexOf("/", 7));
		String curParentURL  = initialURL.substring(0, initialURL.lastIndexOf("/"));
		if(relativeURL.startsWith("/")){
			//相对于网站根目录的地址
			return rootURL + relativeURL;
		} if(relativeURL.startsWith("http://")){
			return relativeURL;
		}else {
			return curParentURL + relativeURL;
		}
	}
	
	public static void readConfig(){
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			//this.getClass().getClassLoader();
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
			}
			createSaveDict();
			System.out.println(nextPage + "---" + nextList + "---" + articleInList);
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
