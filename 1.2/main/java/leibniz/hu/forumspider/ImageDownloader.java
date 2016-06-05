package leibniz.hu.forumspider;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import us.codecraft.webmagic.downloader.HttpClientDownloader;

public class ImageDownloader extends HttpClientDownloader {
	@Override
	protected String getContent(String charset, HttpResponse httpResponse) throws IOException {
		byte[] imageByte = EntityUtils.toByteArray(httpResponse.getEntity());
		String iageStr = SpiderUtils.GetImageStr(imageByte);
		return iageStr;
	}
}