# ForumSpider

##简介
&nbsp;&nbsp;&nbsp;&nbsp;这是一个简单的抓取论坛指定帖子里所有照片的小Java爬虫。

&nbsp;&nbsp;&nbsp;&nbsp;抓取的规则为帖子标题的关键字匹配，无模糊查询无拆词/语义分析，仅判断是否包含关键词。

> P.S. 遇到重名的图片会先判断图片是否完整，若完整则跳过下载，否则重新下载。


##使用
&nbsp;&nbsp;&nbsp;&nbsp;爬虫由src目录下的spider.cfg.xml进行配置，具体配置参见该xml中的注释。

&nbsp;&nbsp;&nbsp;&nbsp;执行`java leibniz.hu.forumspider.Spider`即可开启爬虫

&nbsp;&nbsp;&nbsp;&nbsp;执行`java leibniz.hu.forumspider.MakeWeb`即可将下载目录里所有图片生成一系列网页以便进行浏览。

##原理
&nbsp;&nbsp;&nbsp;&nbsp;主线程负责遍历帖子列表（从用户再xml中设定的初始页面开始），找到符合关键词要求的帖子，并将帖子标题及URL放入一个List<Map\>，如果帖子解析器线程数量少于5个（刚启动爬虫的时候），会帮忙开启帖子解析器。

&nbsp;&nbsp;&nbsp;&nbsp;帖子解析器(leibniz.hu.forumspider.ArticleScanThread类)从上述的List中获取帖子解析任务，打开帖子找到需要下载的照片地址，将每个找到的照片地址放入图片下载等待队列List，由图片下载线程(leibniz.hu.forumspider.ImageDownThread类)进行处理（2016.01.30新增）。由线程管理器管理解析器的数量。

&nbsp;&nbsp;&nbsp;&nbsp;图片下载线程与帖子解析器类似，从图片下载任务List中获得下载任务，并由线程管理器管理下载器的数量。

&nbsp;&nbsp;&nbsp;&nbsp;目前单独分离出一个线程专门用于管理帖子解析器/图片下载线程（2016.02.02新增），若任务数量比解析器数量多N倍时，会自动开启新的解析器线程；当任务被处理差不多，List中任务较少时，会关闭当前线程（但会保证某个最低的解析器线程数量）；并增加了连接异常重试机制（2016.02.15新增，最多尝试5次）。

##下一步工作
&nbsp;&nbsp;&nbsp;&nbsp;目前查找帖子和查找下一页的正则表达式是从xml配置文件中读取的（2016.02.03修改），下一步要多找几个网站总结链接地址的规律，进一步扩展这个爬虫。
