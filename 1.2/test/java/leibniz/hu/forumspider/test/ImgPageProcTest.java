package leibniz.hu.forumspider.test;

import org.junit.Test;

import leibniz.hu.forumspider.ImageToLocalPipeline;
import leibniz.hu.forumspider.ImgPageProcessor;
import us.codecraft.webmagic.Spider;

public class ImgPageProcTest {
	public static void main(String[] args){
		startSpider();
	}
	@Test
	public static void startSpider() {
		Spider.create(new ImgPageProcessor()).addPipeline(new ImageToLocalPipeline("d:\\StackFlow\\91new\\"))/*.setDownloader(new ImageDownloader())*/
			.addUrl("http://f.k6p.co/forumdisplay.php?fid=19").thread(20).run();
		Spider.create(new ImgPageProcessor()).addPipeline(new ImageToLocalPipeline("d:\\StackFlow\\91new\\"))/*.setDownloader(new ImageDownloader())*/
			.addUrl("http://f.k6p.co/forumdisplay.php?fid=21").thread(20).run();
	}
}
