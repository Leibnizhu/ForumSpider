# ForumSpider
这是一个简单的抓取论坛指定帖子里所有照片的小Java爬虫。


抓取的规则为帖子标题的关键字匹配，无模糊查询无拆词/语义分析，仅判断是否包含关键词。


由src目录下的spider.cfg.xml进行配置，具体配置参见该xml中的注释。


目前查找帖子和查找下一页的正则表达式是写死的，下一步要多找几个网站总结链接地址的规律，进一步扩展这个爬虫
