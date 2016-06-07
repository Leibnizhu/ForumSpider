package leibniz.hu.forumspider.test;

import org.junit.Test;

import leibniz.hu.forumspider.SpiderStarter;

public class ImgPageProcTest {
	@Test
	public void start() {
		new SpiderStarter().startSpider("D:\\Stackflow\\91porn57.properties");
	}
}
