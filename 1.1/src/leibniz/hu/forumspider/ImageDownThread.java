package leibniz.hu.forumspider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Map;

public class ImageDownThread extends SpiderBinaryDownloader implements Runnable {
	private String imageURL;
	private String saveDictionary;
	public static int downloadingImgNum = 0;
	public static int downloadedImgNum = 0;

	@Override
	public boolean isNeedReDownload(RandomAccessFile raf){
		raf.seek(raf.length()-2);
		if(raf.read() == 0xff){
			raf.seek(raf.length()-1);
			if(raf.read() == 0xd9){
				raf.close();
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	};

	@Override
	public void run() {
		Map<String, String> tempMission = null;
		while(true){
			if(Spider.getSpiderInstance().getImageDownList().size() > 0){
				//每次从待处理队列中取出一个任务
				tempMission = Spider.getSpiderInstance().getImageDownList().remove(0);
				//得到新任务的url及标题（保存路径）
				this.imageURL = tempMission.get("imageDownURL");
				this.saveDictionary = tempMission.get("saveDictionary").replaceAll("[#<>/\\]", "");
				downloadingImgNum++;
				if(download(imageURL, saveDictionary, 0)){
					//下载成功
					downloadedImgNum++;
				}
				downloadingImgNum--;
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
