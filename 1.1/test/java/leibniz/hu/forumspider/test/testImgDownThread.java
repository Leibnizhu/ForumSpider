package leibniz.hu.forumspider.test;

import static org.junit.Assert.*;

import leibniz.hu.forumspider.ImageDownThread;

import org.junit.Test;

public class testImgDownThread {

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@Test
	public void testImgDown(){
		ImageDownThread.readConfig(null);
		new ImageDownThread().downImage("http://k1.433200.net/1/20151127/52d78f905eb63d1e224.jpg", "/home/leibniz/Workspaces/MyEclipse 2015/ForumSpider", 0);
	}
}
