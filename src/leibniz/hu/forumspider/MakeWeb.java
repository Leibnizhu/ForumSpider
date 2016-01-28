package leibniz.hu.forumspider;

public class MakeWeb {
	private String initialURL;
	private String savepath;
	private ArrayList<String> keywords = new ArrayList<String>();
	
	public static void main(String[] args){
			new Spider().readConfig();
			List<String> imgPaths = new LinkedList<String>();
			File curDir = new File(initPath);
			//遍历得到文件夹下所有文件的路径+文件名(List)
			ergodicSubDir(imgPaths, curDir);
			//开始生成HTML
			PrintWriter prHtml = new PrintWriter(new BufferedWriter(new FileWriter(initPath + "index.html", false)));
			prHtml.println("<html><head><title>" + initPath + "</title></head><body align=center>");
			for(String imgPath: imgPaths){
				prHtml.println("<img src=\"" + imgPath + "\"/>")；
			}
			prHtml.println("</body></html>");
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