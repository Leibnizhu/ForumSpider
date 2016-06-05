package leibniz.hu.forumspider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;

public class ImageToLocalPipeline extends FilePersistentBase implements Pipeline {

	public ImageToLocalPipeline(String path) {
		if (!path.endsWith("\\")) {
			path += "\\";
		}
		setPath(path);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void process(ResultItems resultItems, Task task) {
		String title = resultItems.get("title");
		if (null == title) {
			// 无标题
			return;
		}
		Object imgURLs = resultItems.get("imgURLs");
		if (null == imgURLs) {
			// 无可下载资源
			return;
		}
		if (!(imgURLs instanceof ArrayList)) {
			// imgURLs类型错误
			return;
		}
		for (String imgURL : (ArrayList<String>) imgURLs) {
			// 获取url中的文件名
			String filename = imgURL.substring(imgURL.lastIndexOf("/") + 1);
			saveImage(imgURL, filename, (this.path + preHandle(title) + "\\"));
		}
	}

	private String preHandle(String filename) {
		filename = filename.replaceAll(" - 91自拍达人原创申请", "");
		filename = filename.replaceAll(" - 我爱我妻", "");
		filename = filename.replaceAll("[\\\\|\\\\/\"@ :\\\\?\\\\*\\\\.<>]", "");	//去掉可能引起路径错误的字符
		return filename;
	}

	private void saveImage(String imgURL, String filename, String path) {
		File pathFile = new File(path);
		if (!pathFile.exists()) {
			// 路径不存在的话先创建
			pathFile.mkdirs();
		}
		// 保存文件到指定路径
		InputStream in = null;
		try {
			File downFile = new File(pathFile, filename);
			//先判断文件是否已存在，如果存在，判断是否完整，完整则不用下载，直接return
			if(downFile.exists()){
				if(!isNeedReDownload(downFile)){
					System.out.println(imgURL + "已存在并文件完整，无需重新下载");
					return;
				}
			}
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpGet httpget = new HttpGet(imgURL);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			in = entity.getContent();
			System.out.println("正在下载：" + imgURL);
			FileOutputStream fout = new FileOutputStream(downFile);
			int len = -1;
			byte[] tmp = new byte[1024];
			while ((len = in.read(tmp)) != -1) {
				fout.write(tmp, 0, len);
			}
			fout.flush();
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(null != in){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean isNeedReDownload(File fImg){
		try {
			RandomAccessFile raf = new RandomAccessFile(fImg, "r");
			//通过判断文件结尾是否为0xff 0xd9来判定图片是否下载完整
			raf.seek(raf.length()-2);
			if(raf.read() == 0xff){
				raf.seek(raf.length()-1);
				if(raf.read() == 0xd9){
					//文件完整，返回false
					raf.close();
					return false;
				}
			}
			//不完整，返回true重新下载
			raf.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
}