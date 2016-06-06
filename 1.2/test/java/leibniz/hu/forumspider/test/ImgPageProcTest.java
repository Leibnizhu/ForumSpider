package leibniz.hu.forumspider.test;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import leibniz.hu.forumspider.ImageToLocalPipeline;
import leibniz.hu.forumspider.ImgPageProcessor;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;

public class ImgPageProcTest {
	private static String propPath = "";
	public static void main(String[] args){
		if(args.length >= 1){
			propPath = args[0];
			new ImgPageProcTest().startSpider();
		} else {
			return;
		}
	}
	@Test
	public void startSpider() {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(propPath));
			System.out.println("从以下网址开始爬虫程序" + prop.getProperty("startURL"));
			Spider.create(new ImgPageProcessor(prop.getProperty("URL_LIST"), prop.getProperty("URL_POST"), prop.getProperty("URL_IMG"), prop.getProperty("POST_IGNORE"), prop.getProperty("TITLE_IGNORE")))
			.addPipeline(new ImageToLocalPipeline(prop.getProperty("storePath")))
			.setScheduler(new FileCacheQueueScheduler(prop.getProperty("storePath")))
			.addUrl(prop.getProperty("startURL")).thread(20).run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void regexTest(){
		String regex = "[\\\\|\\\\/\"@ :\\\\?\\\\*\\\\.<>]";
		String test = "AA\\/:*?\"<>| .";
		System.out.println(test.replaceAll(regex, ""));
	}
}
