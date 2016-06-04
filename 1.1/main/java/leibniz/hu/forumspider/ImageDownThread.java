package leibniz.hu.forumspider;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

public class ImageDownThread extends SpiderBinaryDownloader implements Runnable {
	private String imageURL;
	private String saveDictionary;
	public static int downloadingImgNum = 0;
	public static int downloadedImgNum = 0;

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

	@Override
	public void run() {
		Map<String, String> tempMission = null;
		while(true){
			if(SpiderMain.getSpiderInstance().getImageDownList().size() > 0){
				//每次从待处理队列中取出一个任务
				tempMission = SpiderMain.getSpiderInstance().getImageDownList().remove(0);
				//得到新任务的url及标题（保存路径）
				if(null != tempMission){
					this.imageURL = tempMission.get("imageDownURL");
					this.saveDictionary = tempMission.get("saveDictionary");
					downloadingImgNum++;
					if(downImage(imageURL, saveDictionary, 0)){
						//下载成功
						downloadedImgNum++;
					} else {
						//下载失败，回炉重造
						SpiderMain.getSpiderInstance().getImageDownList().add(tempMission);
					}
					if(downloadingImgNum>0){
						downloadingImgNum--;
					}
				}
			}
			//处理完一个下载任务，休眠一段时间
			//一方面防反爬，另一方面方便ThreadManager判断是否可以关闭线程
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				//由ThreadManager发出的中断，终止当前进程
				break;
			}
		}
	}
}