package leibniz.hu.forumspider.test;

import org.junit.Test;

import leibniz.hu.forumspider.ImageToLocalPipeline;
import leibniz.hu.forumspider.ImgPageProcessor;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;

public class ImgPageProcTest {
	private static String startURL = "";
	public static void main(String[] args){
		if(args.length >= 1){
			startURL = args[0];
		}
		new ImgPageProcTest().startSpider();
	}
	@Test
	public void startSpider() {
		System.out.println("从以下网址开始爬虫程序" + startURL);
		Spider.create(new ImgPageProcessor()).addPipeline(new ImageToLocalPipeline("d:\\StackFlow\\91new\\")).setScheduler(new FileCacheQueueScheduler("d:\\StackFlow\\"))/*.setDownloader(new ImageDownloader())*/
			.addUrl(startURL).thread(20).run();
	}
	
	@Test
	public void regexTest(){
		String regex = "[\\\\|\\\\/\"@ :\\\\?\\\\*\\\\.<>]";
		String test = "AA\\/:*?\"<>| .";
		System.out.println(test.replaceAll(regex, ""));
	}
}
