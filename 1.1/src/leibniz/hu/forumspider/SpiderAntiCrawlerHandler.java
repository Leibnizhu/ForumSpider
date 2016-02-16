package leibniz.hu.forumspider;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpiderAntiCrawlerHandler extends SpiderConfigurator{
	private static Map<String,String> cookieMap = new HashMap<String, String>();
	
	//模拟请求头部信息，防反爬
	public static void initReqHeader(URLConnection conn, String refURL){
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36");
		conn.setRequestProperty("Cache-control","no-cache, no-store");
		conn.setRequestProperty("DNT", "1");
		//根据形参填充参考页面参数
		if(null != refURL){
			conn.setRequestProperty("Referer", refURL);
		}
		conn.setRequestProperty("Accept-Encoding", "deflate, sdch");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
		//根据本类维护的cookieMap，构造出cookies请求部分
		Set<String> cookieKeys = cookieMap.keySet();
		if(null != cookieKeys){
			for(String key:cookieKeys){
				conn.addRequestProperty("cookie", key + "=" + cookieMap.get(key));
			}
		}
		conn.setConnectTimeout(30*1000);  
	    conn.setReadTimeout(30*1000);
	}
		
	//从connection的响应中获取cookie设定信息，更新原有cookieMap
	public static void getCookie(URLConnection conn){
		List<String> listTemp= conn.getHeaderFields().get("Set-Cookie");
		if(null != listTemp){
			for(String cookie : listTemp){
				String strTemp = cookie.split(";")[0];
				//因为是Map,旧的Cookie会被替代
				cookieMap.put(strTemp.split("=")[0], strTemp.split("=")[1]);
			}
		}
		//参照chrome发出的cookies，两个可有可无的cookies
		if(null == cookieMap.get("AJSTAT_ok_times")){
			cookieMap.put("AJSTAT_ok_times", "1");
		}
		if(null == cookieMap.get("AJSTAT_ok_pages")){
			cookieMap.put("AJSTAT_ok_pages", "1");
		} else {
			cookieMap.put("AJSTAT_ok_pages", String.valueOf((Integer.parseInt(cookieMap.get("AJSTAT_ok_pages")) + 1)));
		}
	}
}
