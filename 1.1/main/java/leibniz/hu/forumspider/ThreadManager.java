package leibniz.hu.forumspider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import leibniz.hu.forumspider.ArticleScanThread;
import leibniz.hu.forumspider.ImageDownThread;
import leibniz.hu.forumspider.ThreadManager;

public class ThreadManager implements Runnable{
	public static int imgDownThreadNum;
	public static int artcScanThreadNum;
	private List<Thread> imgDownList;
	private List<Thread> artcScanList;
	
	public void run() {
		String prevMsg = null;
		while(true){
			ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
			int threadNum = currentGroup.activeCount();
			Thread[] threadList = new Thread[threadNum];
			currentGroup.enumerate(threadList);
			imgDownList = new ArrayList<Thread>();
			artcScanList = new ArrayList<Thread>();
			
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
			String msg = "帖子解析器：" + artcScanThreadNum + "个；待处理帖子：" + SpiderMain.getSpiderInstance().getUnHandleList().size() + "个。"
					+ "图片下载器：" + imgDownThreadNum + "个；待下载:" + SpiderMain.getSpiderInstance().getImageDownList().size() + "；下载中:" + ImageDownThread.downloadingImgNum  + "；已下载:" + ImageDownThread.downloadedImgNum;
			if(!msg.equals(prevMsg)){
				System.out.println(msg);
				prevMsg = msg;
			}
			//调整线程数量
			adjustArtcScanThread();
			adjustImgDownThread();
			
			//休眠，降低唤醒频率
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void adjustArtcScanThread(){
		int count = 0;
		//根据待处理帖子数和帖子分析器数量动态分配线程数量
		while((artcScanThreadNum <= 10) || ((SpiderMain.getSpiderInstance().getUnHandleList().size() / (float)artcScanThreadNum) >= 1.5)){
			Thread temp = new Thread(new ArticleScanThread(), "articleScan-"+ (new Random()).nextInt());
			temp.start();
			artcScanList.add(temp);
			artcScanThreadNum = artcScanList.size();
		}
		while(artcScanThreadNum >= 15 && ((SpiderMain.getSpiderInstance().getUnHandleList().size() / (float)artcScanThreadNum) <= 0.75)){
			Thread curArtcScanThread = artcScanList.remove(0);
			if(Thread.State.TIMED_WAITING == curArtcScanThread.getState()){
				//线程处于sleep()方法中，说明已完成一项任务，可以结束
				curArtcScanThread.interrupt();
				count++;
				if(count >= 5){
					break;
				}
			} else {
				//否则放回队列
				artcScanList.add(curArtcScanThread);
			}
			artcScanThreadNum = artcScanList.size();
		}
	}
	
	public void adjustImgDownThread(){
		int count = 0;
		//根据待下载图片数和图片下载器数量动态分配线程数量
		while((imgDownThreadNum <= 25) || ((SpiderMain.getSpiderInstance().getImageDownList().size() / (float)imgDownThreadNum) >= 1.5)){
			Thread temp = new Thread(new ImageDownThread(), "imageDown-"+ (new Random()).nextInt());
			temp.start();
			imgDownList.add(temp);
			imgDownThreadNum = imgDownList.size();
		}
		while((imgDownThreadNum >= 50) && ((SpiderMain.getSpiderInstance().getImageDownList().size() / (float)imgDownThreadNum) <= 0.5)){
			Thread curImgScanThread = imgDownList.remove(0);
			if(Thread.State.TIMED_WAITING == curImgScanThread.getState()){
				//线程处于sleep()方法中，说明已完成一项任务，可以结束
				curImgScanThread.interrupt();
				count++;
				if(count >= 10){
					break;
				}
			} else {
				//否则放回队列
				imgDownList.add(curImgScanThread);
			}
			imgDownThreadNum = imgDownList.size();
		}
	}
	
	//判断是否还有ThreadManager进程
	public static boolean isManagerDie(){
		ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
		int threadNum = currentGroup.activeCount();
		Thread[] threadList = new Thread[threadNum];
		currentGroup.enumerate(threadList);
		
		//遍历
		for (int i = 0; i < threadNum; i++) {
			if(threadList[i].getName().startsWith("manager")){
				return false;
			}
		}
		return true;
	}
	
	public static void managerGuard(){
		if(ThreadManager.isManagerDie()){
			System.out.println("------------------------>ThreadManager挂掉了，重新启动一个....");
			new Thread(new ThreadManager(), "manager-"+ (new Random()).nextInt()).start();
		}
	}
}
