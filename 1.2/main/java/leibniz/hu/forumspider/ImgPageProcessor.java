package leibniz.hu.forumspider;

import java.util.ArrayList;
import java.util.List;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class ImgPageProcessor implements PageProcessor {
	// 抓取网站的相关配置，包括编码、抓取间隔、重试次数等
	private Site site = Site.me().setCharset("utf-8").setRetryTimes(5).setSleepTime(2000).setUserAgent(
			"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36");
	// 帖子列表地址的正则表达式
	public static final String URL_LIST = "http://f.k6p.co/forumdisplay.php\\?fid=\\d+&page=\\d+";
	// 帖子地址的正则表达式
	public static final String URL_POST = "(http://f.k6p.co/viewthread.php\\?tid=\\d+&(.*))";
	// 图片地址的正则表达式
	public static final String URL_IMG = "<img src=\"images/common/none.gif\" file=\"(attachments/\\w+.jpe?g)";

	@Override
	public Site getSite() {
		return this.site;
	}

	/*
	 * 定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	 */
	@Override
	public void process(Page page) {
		String curURL = page.getUrl().toString();
		// 根据页面的不同类型分别进行对应操作
		if (curURL.contains("forumdisplay.php")) {
			// 帖子列表页，找下一页帖子列表页和帖子地址
			page.addTargetRequests(page.getHtml().links().regex(URL_LIST).all());
			page.addTargetRequests(page.getHtml().links().regex(URL_POST).all());
		} else if (curURL.contains("viewthread.php")) {
			// 帖子页，找下一页帖子和图片地址
			page.addTargetRequests(page.getHtml().links().regex(URL_POST).all());
			List<String> relImgURL = page.getHtml().regex(URL_IMG).all();
			if (null != relImgURL && relImgURL.size() > 0) {
				List<String> absImgURL = new ArrayList<String>();
				for (String imgURL : relImgURL) {
					absImgURL.add(SpiderUtils.relativeURLHandler(curURL, imgURL));
				}
				// 将要下载的图片地址和标题放入Field
				String title = page.getHtml().css("title", "text").get().replace(" - 91自拍论坛 - Powered by Discuz!", "");
				page.putField("title", title);
				page.putField("imgURLs", absImgURL);
			}
		} else {
			// 其他，忽略
		}
	}

}
