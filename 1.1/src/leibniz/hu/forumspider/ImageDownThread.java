package leibniz.hu.forumspider;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

public class ImageDownThread extends SpiderBinaryDownloader implements Runnable {
	private String imageURL;
	private String saveDictionary;
	public static int downloadingImgNum = 0;
	public static int downloadedImgNum = 0;

	public boolean isNeedReDownload(RandomAccessFile raf){
		try {
			//通过判断文件结尾是否为0xff 0xd9来判定图片是否下载完整
			raf.seek(raf.length()-2);
			if(raf.read() == 0xff){
				raf.seek(raf.length()-1);
				if(raf.read() == 0xd9){
					raf.close();
					return false;
				} else {
					//不完整则返回true重新下载，否则返回false
					return true;
				}
			} else {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void run() {
		Map<String, String> tempMission = null;
		while(true){
			if(SpiderMain.getSpiderInstance().getImageDownList().size() > 0){
				//每次从待处理队列中取出一个任务
				tempMission = SpiderMain.getSpiderInstance().getImageDownList().remove(0);
				//得到新任务的url及标题（保存路径）
				this.imageURL = tempMission.get("imageDownURL");
				this.saveDictionary = tempMission.get("saveDictionary").replaceAll("[#<>!]", "");
				downloadingImgNum++;
				if(download(imageURL, saveDictionary, 0)){
					//下载成功
					downloadedImgNum++;
				}
				if(downloadingImgNum>0){
					downloadingImgNum--;
				}
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}