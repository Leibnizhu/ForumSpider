package leibniz.hu.forumspider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SpiderConfigurator {
	public static String initialURL = null;
	public static String savepath = null;
	public static String nextPage = null;
	public static String nextList = null;
	public static String articleInList = null;
	public static String imgAddr = null;
	public static int minPageSize = 0;
	public static ArrayList<String> keywords = new ArrayList<String>();
		
	//从spider.cfg.xml中读取配置
	public static void readConfig(String xmlPath){
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			if(null == xmlPath){
				xmlPath = "spider.cfg.xml";
			}
			Document document = db.parse(ClassLoader.getSystemResource(xmlPath).toString());
			
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
				//找到帖子中图片地址的正则表达式，放入属性
				if("min-page-size".equals(tempNodeName)){
					minPageSize = tempNode.getTextContent();
				}
			}
			createSaveDict();
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
