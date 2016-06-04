package leibniz.hu.forumspider;

public class SpiderUtils extends SpiderConfigurator{
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
