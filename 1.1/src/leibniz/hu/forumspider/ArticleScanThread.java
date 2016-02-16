package leibniz.hu.forumspider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArticleScanThread implements Runnable {
	//帖子URL
	private String articleURL;
	//保存的文件夹路径（不包含帖子标题的子文件夹）
	/* private String originDictionary = SpiderUtils.savepath; */
	//保存的文件夹路径（包含帖子标题的子文件夹）
	private String saveDictionary;
	//private String nextPageRegax;
	//private String imgAddrRegax;
	
	@Override
	public void run() {
		Map<String, String> tempMission = null;
		/* nextPageRegax = SpiderUtils.nextPage;
		imgAddrRegax = SpiderUtils.imgAddr; */
		while(true){
			ThreadManager.managerGuard();
			
			if(Spider.getSpiderInstance().getUnHandleList().size() > 0){
				tempMission = Spider.getSpiderInstance().getUnHandleList().remove(0);
				//得到新任务的url及标题（保存路径）
				this.articleURL = tempMission.get("url");
				this.saveDictionary = savepath +"/" + tempMission.get("title");
				String curURL = articleURL;
				String refURL = null;
				
				//图片链接的正则表达式
				Pattern pImageLink = Pattern.compile(imgAddr);
				//下一页链接的正则表达式
				Pattern pNextLink = Pattern.compile(nextPage);
				//标识是否有下一页
				boolean nextFlag ;
				
				while(true){
					//System.out.println(new Date() + " 正在处理帖子《" + tempMission.get("title") + "》的新一页："  + curURL);
					nextFlag = false;
					
					//先直接读取整个页面
					String strHtml =  /* SpiderUtils. */downHtml(curURL, refURL, 0);
	                //System.out.println(new Date() + " 帖子《" + tempMission.get("title") + "》下载完毕，共计" + strHtml.length() + "字节。开始解析图片地址……");
	                
	                //匹配到图片链接
	                Matcher mImageLink = pImageLink.matcher(strHtml);
	                while(mImageLink.find()){
	                	//创建保存图片的子文件夹
	                	File saveDict = new File(saveDictionary);
	                	if(!saveDict.exists()){
	                		saveDict.mkdirs();
	                	}
	                	//放入待处理队列
            			Map<String, String> tempResult = new HashMap<String, String>(); 
            			tempResult.put("imageDownURL", SpiderUtils.relativeURLHandler(mImageLink.group(1)));
            			tempResult.put("saveDictionary", saveDictionary);
            			Spider.getSpiderInstance().getImageDownList().add(tempResult);
	                }
	                
	                //匹配到下一页的链接
	                Matcher mNextLink = pNextLink.matcher(strHtml);
	                if(mNextLink.find()){
	                	//记录来源页面
	                	refURL = curURL;
	                	curURL = SpiderUtils.relativeURLHandler(mNextLink.group(1).replace("&amp;", "&"));
	                	nextFlag = true;
	                }
					//还是没找到下一页的话，退出循环
					if(nextFlag == false){
						break;
					}
				}
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				break;  
			}
		}
	}
}
