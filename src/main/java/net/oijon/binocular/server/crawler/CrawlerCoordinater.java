package net.oijon.binocular.server.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.oijon.binocular.server.Banlist;
import net.oijon.binocular.server.Main;
import net.oijon.utils.logger.Log;

public class CrawlerCoordinater {
	
	private Log log = Main.getLog();
	private ArrayList<Crawler> crawlerList = new ArrayList<Crawler>();
	private ArrayList<URL> coordinatedReadLater = new ArrayList<URL>();
	private ArrayList<Thread> threadList = new ArrayList<Thread>();
	private Banlist banlist = new Banlist();
	
	public CrawlerCoordinater(int amountOfCrawlers) {
		for (int i = 0; i < amountOfCrawlers; i++) {
			Crawler crawler = new Crawler(i, banlist, coordinatedReadLater);
			crawlerList.add(crawler);
			Thread t = new Thread(crawlerList.get(i));
			threadList.add(t);
		}
	}
	
	// TODO: make sure to add the ability to change the amount of crawlers active at once
	
	public void run() {
		Runnable crawlerTimer = new Runnable() {
			public void run() {
				// make them parse
				for (int i = 0; i < crawlerList.size(); i++) {
					if (crawlerList.get(i).readLaterSize() > 0 & !crawlerList.get(i).isScanning()) {
						log.info("Links found in read later! Parsing now...");
						log.debug(crawlerList.get(i).readLaterSize() + " found...");
						
						Thread t = threadList.get(i);
						if (!t.isAlive()) {
							t = new Thread(crawlerList.get(i));
							threadList.set(i, t);
							t.start();
						}
					}
				}
				
				// distribute from main
				while (coordinatedReadLater.size() > 0) {
					// originally used modulus, but that made crawler 0 most likely to parse
					int crawlerID = (int) (Math.random() * crawlerList.size());
					Crawler c = crawlerList.get(crawlerID);
					
					log.debug("Crawler " + c.id  + " has been called to parse " + coordinatedReadLater.get(0));
					if (!banlist.hasBanned(coordinatedReadLater.get(0))) {
						c.queue(coordinatedReadLater.get(0));
					}
					coordinatedReadLater.remove(0);
				}
			}
		};
		
		Runnable listInfo = new Runnable() {
			
			@Override
			public void run() {
				log.info("Total in coordinated readlater: " + coordinatedReadLater.size());
				
				for (int i = 0; i < crawlerList.size(); i++) {
					log.debug("Total in Crawler " + i + " readlater: " + crawlerList.get(i).readLaterSize());
				}
			}
		};
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
		executor.scheduleAtFixedRate(crawlerTimer, 0, 1, TimeUnit.SECONDS);
		executor.scheduleAtFixedRate(listInfo, 0, 30, TimeUnit.SECONDS);
	}
	
	public void add(String add) {
		try {
			coordinatedReadLater.add(new URL(add));
		} catch (MalformedURLException e) {
			log.err("Unable to add URL " + add + " - " + e.toString());
		}
	}
	
	public void add(URL add) {
		coordinatedReadLater.add(add);
	}

}
