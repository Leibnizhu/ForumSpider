package leibniz.hu.forumspider;

import java.util.ArrayList;


public class DownloadThread implements Runnable {

	private String articleURL;
	
	public DownloadThread(ArrayList<String> unHandleList) {
		super();
		this.articleURL = unHandleList.remove(0);
	}

	@Override
	public void run() {
		System.out.println(Thread.currentThread() + "," + articleURL);
		
	}

}
