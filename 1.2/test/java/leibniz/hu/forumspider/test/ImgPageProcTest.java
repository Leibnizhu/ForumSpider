package leibniz.hu.forumspider.test;

import org.junit.Test;

import leibniz.hu.forumspider.ImgPageProcessor;
import us.codecraft.webmagic.Spider;

public class ImgPageProcTest {
	@Test
	public void startSpider(){
		Spider.create(new ImgPageProcessor()).addUrl("http://f.k6p.co/forumdisplay.php?fid=19&page=2").thread(20).run();
	}
}
