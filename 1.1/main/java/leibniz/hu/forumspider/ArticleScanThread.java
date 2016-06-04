package leibniz.hu.forumspider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import leibniz.hu.forumspider.SpiderMain;
import leibniz.hu.forumspider.SpiderUtils;
import leibniz.hu.forumspider.ThreadManager;


public class ArticleScanThread extends SpiderHtmlDownloader implements Runnable {
	//帖子URL
	private String articleURL;
	//保存的文件夹路径（包含帖子标题的子文件夹）
	private String saveDictionary;
	
	@Override
	public void run() {
		Map<String, String> tempMission = null;
		while(true){
			ThreadManager.managerGuard();
			
			if(SpiderMain.getSpiderInstance().getUnHandleList().size() > 0){
				tempMission = SpiderMain.getSpiderInstance().getUnHandleList().remove(0);
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
					String strHtml = downHtml(curURL, refURL, 0);
	                //System.out.println(new Date() + " 帖子《" + tempMission.get("title") + "》下载完毕，共计" + strHtml.length() + "字节。开始解析图片地址……");
	                
					if(null != strHtml && strHtml.length() > minPageSize){
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
							tempResult.put("saveDictionary", saveDictionary.replaceAll("[#<>!]", ""));
							SpiderMain.getSpiderInstance().getImageDownList().add(tempResult);
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
					//处理完当前帖子的一页，休眠一段时间，防反爬
					//如果读到页面为空，也会跳过解析阶段，到这里休眠后重新读取
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						//由ThreadManager发出的中断，无视
					}
				}
			}
			//处理完一个帖子，休眠一段时间
			//一方面防反爬，另一方面方便ThreadManager判断是否可以关闭线程
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				//由ThreadManager发出的中断，终止当前进程
				break;  
			}
		}
	}
}
