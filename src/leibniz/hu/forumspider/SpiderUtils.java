package leibniz.hu.forumspider;

import java.net.URLConnection;

public class SpiderUtils {
	public static void initReqHeader(URLConnection conn, String refURL){
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Referer", refURL);
	}
	
	public static void makeWeb(String initPath){
			List<String> imgPaths = new LinkedList<String>();
			File curDir = new File(initPath);
			//遍历得到文件夹下所有文件的路径+文件名(List)
			ergodicSubDir(imgPaths, curDir);
			//开始生成HTML
			BufferedWriter brHtml = new BufferedWriter(new FileWriter(initPath + "index.html", false));
			
	}
	
	public static void ergodicSubDir(List<String> imgPaths, File curDir){
		Files[] subFiles = curDir.listFiles();
		for(File subFile: subFiles){
			if(subFile.isDictionary()){
				//是目录，遍历之
				ergodicSubDir(imgPaths, subFile);
			} else if(subFile.isFile()) {
				//是文件，添加之
				imgPaths.add(subFile.getCanonicalPath());
			}
		}
	}
}
