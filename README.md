# ForumSpider
***
##简介
&nbsp;&nbsp;&nbsp;&nbsp;这是一个简单的抓取论坛指定帖子里所有照片的小Java爬虫。

&nbsp;&nbsp;&nbsp;&nbsp;抓取的规则为帖子标题的关键字匹配，无模糊查询无拆词/语义分析，仅判断是否包含关键词。

> P.S. 遇到重名的图片会跳过不下载。
***
##使用
&nbsp;&nbsp;&nbsp;&nbsp;爬虫由src目录下的spider.cfg.xml进行配置，具体配置参见该xml中的注释。

&nbsp;&nbsp;&nbsp;&nbsp;执行`java leibniz.hu.forumspider.Spider`（含主函数）即可开启爬虫
***
##原理
&nbsp;&nbsp;&nbsp;&nbsp;主线程负责遍历帖子列表（从用户再xml中设定的初始页面开始），找到符合关键词要求的帖子，并将帖子标题及URL放入一个List<Map\>，如果帖子解析器线程数量少于5个（刚启动爬虫的时候），会帮忙开启帖子解析器。

&nbsp;&nbsp;&nbsp;&nbsp;帖子解析器(leibniz.hu.forumspider.ArticleScanThread类)从上述的List中获取帖子解析任务，打开帖子找到需要下载的照片地址，对每个找到的照片地址会启动一个图片下载线程(leibniz.hu.forumspider.ImageDownThread类)进行处理。每个线程会不断循环从List中获取任务，若任务数量比解析器数量多N倍时，会自动开启新的解析器线程；当任务被处理差不多，List中任务较少时，会关闭当前线程（但会保证某个最低的解析器线程数量）。

&nbsp;&nbsp;&nbsp;&nbsp;图片下载线程则属于只下载一个图片就结束的线程。后期可以改为帖子解析器将解析到的图片URL及对应标题放入一个图片下载任务List，图片下载线程则维持合理的数量去从List中获取下载任务。

&nbsp;&nbsp;&nbsp;&nbsp;当然也可以单独分离出一个线程专门用于管理帖子解析器/图片下载线程。
***
##下一步工作
&nbsp;&nbsp;目前查找帖子和查找下一页的正则表达式是写死的，下一步要多找几个网站总结链接地址的规律，进一步扩展这个爬虫。