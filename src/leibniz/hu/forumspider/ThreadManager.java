package leibniz.hu.forumspider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ThreadManager implements Runnable{
	public static int imgDownThreadNum;
	public static int artcScanThreadNum;
	
	public void run() {
		while(true){
			ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
			int threadNum = currentGroup.activeCount();
			Thread[] threadList = new Thread[threadNum];
			currentGroup.enumerate(threadList);
			List<Thread> imgDownList = new ArrayList<Thread>();
			List<Thread> artcScanList = new ArrayList<Thread>();
			
			//遍历，查找所有帖子分析和图片下载线程数量
			for (int i = 0; i < threadNum; i++) {
				if(threadList[i].getName().startsWith("articleScan")){
					artcScanList.add(threadList[i]);
				} else if(threadList[i].getName().startsWith("imageDown")){
					imgDownList.add(threadList[i]);
				}
			}
			artcScanThreadNum = artcScanList.size();
			imgDownThreadNum = imgDownList.size();
			System.out.println("帖子解析器：" + artcScanThreadNum + "个；待处理帖子：" + Spider.unHandleList.size() + "个。"
									+ "图片下载器：" + imgDownThreadNum + "个；待下载图片：。" + Spider.imageDownList.size() + "张。");
			
			//根据待处理帖子数和帖子分析器数量动态分配线程数量
			while(artcScanThreadNum <= 10 || (Spider.unHandleList.size() / artcScanThreadNum >= 1.5)){
				Thread temp = new Thread(new ArticleScanThread(), "articleScan-"+ (new Random()).nextInt());
				temp.start();
				artcScanList.add(temp);
				artcScanThreadNum = artcScanList.size();
			}
			while(artcScanThreadNum >= 15 && (Spider.unHandleList.size() / artcScanThreadNum <= 0.75)){
				while(true){
					Thread curArtcScanThread = artcScanList.remove(0);
					if(Thread.State.TIMED_WAITING == curArtcScanThread.getState()){
						//线程处于sleep()方法中，说明已完成一项任务，可以结束
						curArtcScanThread.interrupt();
						break;
					} else {
						//否则放回队列
						artcScanList.add(curArtcScanThread);
					}
				}
				artcScanThreadNum = artcScanList.size();
			}
			
			//根据待下载图片数和图片下载器数量动态分配线程数量
			while(imgDownThreadNum <= 25 || (Spider.imageDownList.size() / imgDownThreadNum >= 1.5)){
				Thread temp = new Thread(new ImageDownThread(), "imageDown-"+ (new Random()).nextInt());
				temp.start();
				imgDownList.add(temp);
				imgDownThreadNum = imgDownList.size();
			}
			while(imgDownThreadNum >= 30 && (Spider.imageDownList.size() / imgDownThreadNum <= 0.5)){
				while(true){
					Thread curImgScanThread = imgDownList.remove(0);
					if(Thread.State.TIMED_WAITING == curImgScanThread.getState()){
						//线程处于sleep()方法中，说明已完成一项任务，可以结束
						curImgScanThread.interrupt();
						break;
					} else {
						//否则放回队列
						imgDownList.add(curImgScanThread);
					}
				}
				imgDownThreadNum = imgDownList.size();
			}
			
			//休眠，降低唤醒频率
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
