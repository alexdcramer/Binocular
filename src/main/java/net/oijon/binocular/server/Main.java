package net.oijon.binocular.server;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.oijon.utils.logger.Log;

public class Main {


	static Log log = new Log(System.getProperty("user.home") + "/Binocular-server");
	static Crawler crawler = new Crawler();
	
	public static void main(String[] args) {
		Connection connection = new Connection();
		connection.start();
		
		log.debug("Started!");
	
		
		Runnable crawlerTimer = new Runnable() {
			public void run() {
				if (Main.getCrawler().getReadLaterLength() != 0) {
					log.info("Links found in read later file! Parsing now...");
					Crawler crawler = Main.getCrawler();
					crawler.parseReadLater();
				}
			}
		};
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(crawlerTimer, 0, 1, TimeUnit.SECONDS);
	}
	
	public static Log getLog() {
		return log;
	}
	
	public static Crawler getCrawler() {
		return crawler;
	}
	
}
