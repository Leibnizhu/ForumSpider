package leibniz.hu.forumspider;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;

public class SpiderStarter {
	private static Logger log = Logger.getLogger(SpiderStarter.class);
	
	public static void main(String[] args){
		if(args.length >= 1){
			new SpiderStarter().startSpider(args[0]);
		} else {
			return;
		}
	}
	
	public void startSpider(String propPath) {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(propPath));
			log.info("从以下网址开始爬虫程序" + prop.getProperty("startURL"));
			Spider.create(new ImgPageProcessor(prop.getProperty("URL_LIST"), prop.getProperty("URL_POST"), prop.getProperty("URL_IMG"), prop.getProperty("POST_IGNORE"), prop.getProperty("TITLE_IGNORE")))
			.addPipeline(new ImageToLocalPipeline(prop.getProperty("storePath")))
			.setScheduler(new FileCacheQueueScheduler(prop.getProperty("storePath")))
			.addUrl(prop.getProperty("startURL")).thread(20).run();
		} catch (IOException e) {
			log.error(SpiderUtils.getTrace(e));
		}
	}
}
