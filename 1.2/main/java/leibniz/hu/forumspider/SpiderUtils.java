package leibniz.hu.forumspider;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.codec.binary.Base64;

public class SpiderUtils{
	//处理相对URl路径，获取绝对URL路径
	public static String relativeURLHandler(String curURL, String relativeURL){
		String rootURL =  curURL.substring(0, curURL.indexOf("/", 7));
		String curParentURL  = curURL.substring(0, curURL.lastIndexOf("/"));
		if(relativeURL.startsWith("/")){
			//相对于网站根目录的地址
			return rootURL + relativeURL;
		} if(relativeURL.startsWith("http://")){
			return relativeURL;
		}else {
			return curParentURL + "/" + relativeURL;
		}
	}
	
	// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		public static String GetImageStr(byte[] data) {
			// 对字节数组Base64编码
			Base64 encoder = new Base64();
			return encoder.encodeToString(data);// 返回Base64编码过的字节数组字符串
		}

		// 对字节数组字符串进行Base64解码并生成图片
		public static boolean GenerateImage(String path, String filename, String imgStr) {
			System.out.println("imgStr == null:" + imgStr == null);
			if (imgStr == null) // 图像数据为空
			{
				return false;
			}
			Base64 decoder = new Base64();
			try {
				// Base64解码
				byte[] b = decoder.decode(imgStr);
				for (int i = 0; i < b.length; ++i) {
					if (b[i] < 0) {// 调整异常数据
						b[i] += 256;
					}
				}
				// 生成jpeg图片
				String imgFilePath = path + filename;// 新生成的图片
				OutputStream out = new FileOutputStream(imgFilePath);
				out.write(b);
				out.flush();
				out.close();
				return true;
			} catch (Exception e) {
				System.out.println("e:" + e.getMessage());
				return false;
			}
		}
}
