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
	public static ArrayList<String> keywords = new ArrayList<String>();
	
	public static void initReqHeader(URLConnection conn, String refURL){
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Referer", refURL);
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
					if(savepath.endsWith("/") || savepath.endsWith("\\")){
						savepath = savepath.substring(0, savepath.length() -1);
					}
				}
			}
			if(null != savepath){
				File saveDictionary = new File(savepath);
				if(!saveDictionary.exists()){
					saveDictionary.mkdirs();
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
