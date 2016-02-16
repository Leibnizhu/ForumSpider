package leibniz.hu.forumspider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class SpiderHtmlDownloader extends SpiderAntiCrawlerHandler{
	//将下载网页独立成方法，便于多次尝试下载
	//tryCnt为尝试的次数，该方法返回读取到的StringBuffer
	public static String downHtml(String curURL, String refURL, int tryCnt){
		Scanner scanner = null;
		try{
			URLConnection conn = new URL(curURL).openConnection();
			initReqHeader(conn, refURL);
			((HttpURLConnection) conn).setRequestMethod("GET");
			//正式发出请求
			conn.connect();
			//获取相应中的cookie
			//SpiderUtils.getCookie(conn);
			StringBuffer bufHtml = new StringBuffer();
			scanner = new Scanner(conn.getInputStream());  
			while (scanner.hasNextLine()) {  
				bufHtml.append(scanner.nextLine());  
			}
			((HttpURLConnection)conn).disconnect();
			return bufHtml.toString();
		} catch (IOException e) {
			tryCnt++;
			if(tryCnt < 5){
				//尝试5次
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {}
				return downHtml(curURL, refURL, tryCnt);
			} else {
				return null;
			}
		} finally {
			//到此读完整个页面，关闭资源
			if(null != scanner){
				scanner.close();
			}
		}
	}
}
