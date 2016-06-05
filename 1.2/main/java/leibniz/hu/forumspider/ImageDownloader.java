package leibniz.hu.forumspider;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import us.codecraft.webmagic.downloader.HttpClientDownloader;

public class ImageDownloader extends HttpClientDownloader {
	//Base64编码
	@Override
	protected String getContent(String charset, HttpResponse httpResponse) throws IOException {
		System.out.println(httpResponse.getHeaders("Content-Type")[0].getValue());
		if("text/html".equals(httpResponse.getHeaders("Content-Type")[0].getValue())){
			return EntityUtils.toString(httpResponse.getEntity(), charset);
		} else {
			return SpiderUtils.GetImageStr(EntityUtils.toByteArray(httpResponse.getEntity()));
		}
	}
}