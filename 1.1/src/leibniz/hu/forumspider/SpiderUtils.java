package leibniz.hu.forumspider;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SpiderUtils {
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
}
