package leibniz.hu.forumspider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class ImgPageProcessor implements PageProcessor {
	private static Set<String> downedImg = new HashSet<String>();
	
	// 抓取网站的相关配置，包括编码、抓取间隔、重试次数等
	private Site site = Site.me().setCharset("utf-8").setRetryTimes(5).setSleepTime(1000).setUserAgent(
			"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36");
	// 帖子列表地址的正则表达式
	private String URL_LIST;
	// 帖子地址的正则表达式
	private String URL_POST;
	// 图片地址的正则表达式
	private String URL_IMG;
	//URL_POST匹配到的URL需要忽略的部分（比如authorid参数，不同的authorid意义不大，还会导致反复访问同一帖子）
	private String POST_IGNORE;
	//标题中忽略的部分，比如后缀的一串字符
	private String TITLE_IGNORE;

	public ImgPageProcessor(String uRL_LIST, String uRL_POST, String uRL_IMG, String pOST_IGNORE, String tITLE_IGNORE) {
		super();
		URL_LIST = uRL_LIST;
		URL_POST = uRL_POST;
		URL_IMG = uRL_IMG;
		POST_IGNORE = pOST_IGNORE;
		TITLE_IGNORE = tITLE_IGNORE;
	}

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
			page.addTargetRequests(page.getHtml().links().regex(URL_POST).replace(POST_IGNORE, "").all());
			List<String> relImgURL = page.getHtml().regex(URL_IMG).all();
			if (null != relImgURL && relImgURL.size() > 0) {
				List<String> absImgURL = new ArrayList<String>();
				for (String imgURL : relImgURL) {
					if(downedImg.contains(imgURL)){
						continue;
					}
					downedImg.add(imgURL);
					absImgURL.add(SpiderUtils.relativeURLHandler(curURL, imgURL));
				}
				// 将要下载的图片地址和标题放入Field
				String title = page.getHtml().css("title", "text").get().replace(TITLE_IGNORE, "");
				page.putField("title", title);
				page.putField("imgURLs", absImgURL);
			}
		} else {
			// 其他，忽略
		}
	}

}
