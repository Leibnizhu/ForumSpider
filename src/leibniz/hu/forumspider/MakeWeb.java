package leibniz.hu.forumspider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MakeWeb {
	public static void main(String[] args){
		//获取配置信息
		Map<String, Object> config = SpiderUtils.readConfig();
		String savepath = (String) config.get("savepath");
		//用于保存遍历到的所有图片文件
		List<String> imgPaths = new LinkedList<String>();
		File curDir = new File(savepath);
		//遍历得到文件夹下所有文件的路径+文件名(List)
		ergodicSubDir(imgPaths, curDir);
		
		//开始生成HTML
		PrintWriter prHtml=null;
		String curTitle= null;
		String prevTitle = null;
		for(int page = 0; page < imgPaths.size()/200 + 1; page++){
			try {
				prHtml = new PrintWriter(new BufferedWriter(new FileWriter(savepath + "/index-" + page + ".html", false)));
				//html例行头部信息，包括标题和编码
				prHtml.println("<html><head><title>" + savepath + "</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head><body align=center>");
				int limit = (page == imgPaths.size()/200)?(imgPaths.size()%200):200;
				for(int i = 0; i < limit; i++){
					//获取照片的绝对路径、相对路径、标题
					String absolutePath = imgPaths.get(page*200+i);
					String relativePath = absolutePath.replaceAll(savepath + "/", "");
					curTitle = relativePath.substring(0,relativePath.lastIndexOf("/"));
					if(i == 0 || !curTitle.equals(prevTitle)){
						//如果是第一张图片，或者标题改变，则输出标题
						prHtml.println("<h2>" + curTitle + "</h2>");
					}
					prHtml.println("<img src=\"" + relativePath + "\"/><br/>");
					prevTitle = curTitle;
				}
				prHtml.println("<a href=\"index-" + (page-1) + ".html\">上一页</a>");
				prHtml.println("<a href=\"index-" + (page+1) + ".html\">下一页</a>");
				prHtml.println("</body></html>");
				prHtml.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				prHtml.close();
			}
		}
	}
	
	public static void ergodicSubDir(List<String> imgPaths, File curDir){
		File[] subFiles = curDir.listFiles();
		for(File subFile: subFiles){
			if(subFile.isDirectory()){
				//是目录，遍历之
				ergodicSubDir(imgPaths, subFile);
			} else if(subFile.isFile()) {
				//是文件，添加之
				if(subFile.getName().contains(".jpg") || subFile.getName().contains(".jpeg") || subFile.getName().contains(".png") || subFile.getName().contains(".gif")){
					try {
						imgPaths.add(subFile.getCanonicalPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}