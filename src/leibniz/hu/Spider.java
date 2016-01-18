package leibniz.hu.forumspider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
	private ArrayList<String> keywords = new ArrayList<String>();
	private ArrayList<String> unHandleList = new ArrayList<String>();
	
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
				//找到初始url的节点，放入类的属性中
				if("starturl".equals(tempNode.getNodeName())){
					initialURL = tempNode.getTextContent();
				}
				//找到下载关键字的节点，将其中所有的关键字加入到类的属性中
				if("keywords".equals(tempNode.getNodeName())){
					NodeList keywordList = tempNode.getChildNodes();
					for (int i = 0; i < keywordList.getLength(); i++) {
		                Node keyword = keywordList.item(i);
		                if("keyword".equals(keyword.getNodeName())){
		                	keywords.add(keyword.getTextContent());
		                }
					}
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
	
	@Test
	public void startSpider(){
		readConfig();
		//System.out.println(initialURL + ", " + keywords);
		BufferedReader brWeb;
		try {
			brWeb = new BufferedReader(new InputStreamReader(new URL(initialURL).openStream()));
			//Regax for email address.
			Pattern pEmail = Pattern.compile("\\w+@\\w+(\\.\\w{1,4})+");
			//Regax for hyper links.
			//<a href="/arthtml/65374.html" target="_blank">收到分公司到法规水电费过</a>
			Pattern pHyperlink = Pattern.compile("<\\s*a\\s*href\\s*=['\"](/arthtml.+?)['\"]\\s*target=\\s*>(.+?)</a>");
			while(true){
				String line = null;
				try {
					while((line = brWeb.readLine()) != null) {
						Matcher mHyperlink = pHyperlink.matcher(line);
						while(mHyperlink.find()){
							System.out.println(mHyperlink.group(0) + "," + mHyperlink.group(1));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		new Spider().startSpider();
	}
}
