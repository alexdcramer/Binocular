package net.oijon.binocular.server;

import java.net.MalformedURLException;
import java.net.URL;
import net.oijon.binocular.server.crawler.CrawlerCoordinater;
import net.oijon.utils.logger.Log;

public class Test {

	private static Log log = Main.getLog();
	public static void main(String[] args) {
		
		try {
			CrawlerCoordinater crawlerCoordinater = new CrawlerCoordinater(8);
			crawlerCoordinater.run();
			URL testURL = new URL("https://reddit.com/");
			log.debug(testURL.getProtocol());
			log.debug(testURL.getAuthority());
			log.debug(testURL.getPath());
			crawlerCoordinater.add(testURL);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.debug("Started!");
	}
	
}
