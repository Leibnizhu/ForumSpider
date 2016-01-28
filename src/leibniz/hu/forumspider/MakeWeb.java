package leibniz.hu.forumspider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class MakeWeb {
	public static void main(String[] args){
			List<String> imgPaths = new LinkedList<String>();
			String savepath = "/media/leibniz/OLD/StackFlow/SpiderDown";
			File curDir = new File(savepath);
			//遍历得到文件夹下所有文件的路径+文件名(List)
			ergodicSubDir(imgPaths, curDir);
			
			//开始生成HTML
			PrintWriter prHtml=null;
			for(int page = 0; page < imgPaths.size()/200 + 1; page++){
				try {
					prHtml = new PrintWriter(new BufferedWriter(new FileWriter(savepath + "/index-" + page + ".html", false)));
					prHtml.println("<html><head><title>" + savepath + "</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head><body align=center>");
					int limit = (page == imgPaths.size()/200)?(imgPaths.size()%200):200;
					//System.out.println(limit);
					for(int i = 0; i < limit; i++){
						String absolutePath = imgPaths.get(page*200+i);
						prHtml.println("<img src=\"" + absolutePath.replaceAll(savepath + "/", "") + "\"/><br/>");
					}
					prHtml.println("<a href=\"index-" + (page-1) + ".html\">上一页</a>");
					prHtml.println("<a href=\"index-" + (page+1) + ".html\">下一页</a>");
					prHtml.println("</body></html>");
					prHtml.flush();
				} catch (IOException e) {
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
				try {
					imgPaths.add(subFile.getCanonicalPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}