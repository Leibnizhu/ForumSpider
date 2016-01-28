package leibniz.hu.forumspider;

class MakeWeb{
	
	public static void main(String initPath){
			List<String> imgPaths = new LinkedList<String>();
			File curDir = new File(initPath);
			//�����õ��ļ����������ļ���·��+�ļ���(List)
			ergodicSubDir(imgPaths, curDir);
			//��ʼ����HTML
			PrintWriter prHtml = new PrintWriter(new BufferedWriter(new FileWriter(initPath + "index.html", false)));
			prHtml.println("<html><head><title>" + initPath + "</title></head><body align=center>");
			for(String imgPath: imgPaths){
				prHtml.println("<img src=\"" + imgPath + "\"/>")��
			}
			prHtml.println("</body></html>");
	}
	
	public static void ergodicSubDir(List<String> imgPaths, File curDir){
		Files[] subFiles = curDir.listFiles();
		for(File subFile: subFiles){
			if(subFile.isDictionary()){
				//��Ŀ¼������֮
				ergodicSubDir(imgPaths, subFile);
			} else if(subFile.isFile()) {
				//���ļ������֮
				imgPaths.add(subFile.getCanonicalPath());
			}
		}
	}
}