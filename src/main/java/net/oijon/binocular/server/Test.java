package net.oijon.binocular.server;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.oijon.utils.logger.Log;

public class Test {

	public static void main(String[] args) {
		Connection connection = new Connection();
		connection.start();
		
		Log log = Main.getLog();
		
		Runnable crawlerTimer = new Runnable() {
			public void run() {
				File file = new File(System.getProperty("user.home") + "/Binocular-server/readlater.txt");
				if (file.length() != 0) {
					log.info("Links found in read later file! Parsing now...");
					Crawler crawler = new Crawler();
					crawler.parseReadLater();
				}
			}
		};
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(crawlerTimer, 0, 10, TimeUnit.SECONDS);
		
		try {
			URL serverURL = new URL("http://localhost:13761/add?https://oijon.net");
			Crawler crawler = new Crawler(serverURL);
			crawler.run();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.debug("Started!");
	}
	
}
