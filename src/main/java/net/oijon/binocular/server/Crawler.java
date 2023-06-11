package net.oijon.binocular.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import net.oijon.utils.logger.Log;

public class Crawler {

	private ArrayList<URL> allowList = new ArrayList<URL>();
	private ArrayList<URL> readlaterList = new ArrayList<URL>();
	private Banlist banlist = new Banlist();
	private URL rootUrl;
	private URL currentUrl;
	Log log = Main.getLog();
	
	private boolean mode = false;
	
	public Crawler() {
		
	}
	
	public Crawler(URL rootUrl) {
		this.rootUrl = rootUrl;
	}
	
	public void makeTags() {
		parseRobotFile();
		for (int i = 0; i < allowList.size(); i++) {
			try {
				currentUrl = allowList.get(i);
				Scanner sc = new Scanner(currentUrl.openStream());
				while(sc.hasNextLine()) {
					
				}
			} catch (IOException e) {
				log.warn("IO exception on creating tags - " + e.toString());
			}
		}
	}
	
	public void run() {
		parseRobotFile();
		parseURL(rootUrl);
		for (int i = 0; i < allowList.size(); i++) {
			if (allowList.get(i).toString().length() < 256) {
				parseURL(allowList.get(i));
			} else {
				allowList.remove(i);
			}
		}
	}
	
	
	
	public void parseURL(URL url) {
		Long beginTime = System.currentTimeMillis();
		try {
			url = new URL(url.toString().split("#")[0]);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		try {
			System.setProperty("http.agent", "Binocular Spider");
			removeFromReadLater(url);
			log.info("Scanning " + url.toString());
			Scanner sc = new Scanner(url.openStream());
			String content = "";
			while (sc.hasNextLine()) {
				content += sc.nextLine() + " ";
			}
				
			//TODO: scan pdfs properly
			ArrayList<String> aTags = new ArrayList<String>();
			String[] aTagList = StringUtils.substringsBetween(content, "<a href=\"", "\"");
			if (aTagList != null) {
				aTags = new ArrayList<String>(Arrays.asList(aTagList));
				log.debug(aTags.size() + " <a> tags found!");
			}
			content = content.replaceAll("(<script)[^&]*(/script>)", " ");
			content = content.replaceAll("(<style)[^&]*(/styles>)", " ");
			content = content.replaceAll("<[^>]*>", " ");
			content = content.replaceAll("\\/", " ");
			//content = content.replaceAll("/", " ");
			content = content.replaceAll("\\.", " ");
			//TODO: check for other whitespace (such as \n)
			String[] splitContent = content.split(" ");
				
			for (int i = 0; i < splitContent.length; i++) {
				if (!splitContent[i].isBlank()) {
					Tag tag = new Tag(splitContent[i]);
					String urlString = url.toString();
					if (urlString.length() < 256) {
						tag.addLink(url);
					}
				}
			}
			
			log.info("Added " + url.toString() + " to " + splitContent.length + " tags.");
				
			for (int i = 0; i < aTags.size(); i++) {
				String substring = "";
				if (!aTags.get(i).isBlank()) {
					String desiredURL = aTags.get(i).split("\\?")[0];
					if (desiredURL.length() >= 4) {
						substring = desiredURL.substring(0, 4);
					}
					if (substring.equals("http")) {
						if (validExtension(new URL(desiredURL))) {
							addToReadLater(new URL(desiredURL));
						}
					}
					else if (desiredURL.length() > 0) {
						if (desiredURL.charAt(0) != '#') {
							if (validExtension(new URL(url, desiredURL))) {
								addToReadLater(new URL(url, desiredURL));
							}
						}
					}
				}
			}
				
			parseReadLater();
			wipeReadLater();
				
			long endTime = System.currentTimeMillis();
			
			long timeTaken = endTime - beginTime;
			long hoursTaken = timeTaken % 3600000;
			timeTaken = timeTaken - (hoursTaken * 3600000);
			long minutesTaken = timeTaken % 60000;
			timeTaken = timeTaken - (minutesTaken * 60000);
			timeTaken = timeTaken / 1000; // timeTaken is now just seconds
			log.info("Processed " + url.toString() + " and all child links in " + hoursTaken + ":" + minutesTaken + ":" + timeTaken);
			
			} catch (IOException e) {
				log.warn("IO exception on URL parse - " + e.toString());
			}
		
	}
	
	public void addToReadLater(URL url) {
		boolean isAlreadyIn = false;
		
		if (mode) {
			File file = new File(System.getProperty("user.home") + "/Binocular-server/readlater.txt");
			if (!file.exists()) {
				try {
					log.warn("No read later file found! Creating one...");
					file.createNewFile();
				} catch (IOException e) {
					log.critical(e.toString());
				}
			}
			
			updateBanlist();
			
			if (!banlist.hasBanned(url)) {
				try {
					try (Scanner scanner = new Scanner(file)) {
						while (scanner.hasNextLine()) {
							String content = scanner.nextLine();
							if (content.equals(url.toString())) {
								isAlreadyIn = true;
								break;
							}
						}
					} catch (FileNotFoundException e) {
						log.warn("No read later file found! Creating one...");
						try {
							file.createNewFile();
						} catch (IOException e1) {
							log.critical(e.toString());
						}
					}
					
					if (!isAlreadyIn) {
						log.debug("Adding link " + url.toString());
						FileWriter fw = new FileWriter(file, true);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(url.toString());
						bw.newLine();
					    bw.close();
					    banlist.add(new BannedURL(url));
					}
				} catch (IOException e) {
					log.err(e.toString());
					e.printStackTrace();
				}
			}
		} else {
			
			updateBanlist();
			if (!banlist.hasBanned(url)) {
				for (int i = 0; i < readlaterList.size(); i++) {
					if (url.equals(readlaterList.get(i))) {
						isAlreadyIn = true;
					}
				}
				
				if (!isAlreadyIn) {
					log.debug("Adding link " + url.toString());
					readlaterList.add(url);
				    banlist.add(new BannedURL(url));
				}
			}
		}
	}
	
	public void parseReadLater() {
		int linksRead = 0;
		if (mode) {
			File file = new File(System.getProperty("user.home") + "/Binocular-server/readlater.txt");
			try (Scanner scanner = new Scanner(file)) {
				while (scanner.hasNextLine()) {
					String content = scanner.nextLine();
					try {
						URL url = new URL(content);
						parseURL(url);
						linksRead++;
					} catch (MalformedURLException e) {
						log.err(e.toString() + " - URL " + content + " in read later file is malformed!");
					}
				}
			} catch (FileNotFoundException e) {
				log.warn("No read later file found! Creating one...");
				try {
					file.createNewFile();
				} catch (IOException e1) {
					log.critical(e.toString());
				}
			}
		} else {
			for (int i = 0; i < readlaterList.size(); i++) {
				parseURL(readlaterList.get(i));
				linksRead++;
			}
		}
		log.info("Parsed " + linksRead + " links!");
	}
	
	public boolean validExtension(URL url) {
		String[] bannedExtensions = {"png", "jpg", "gif", "pdf", "webp", "mp4", "mp3", "css", "js"};
		
		for (int i = 0; i < bannedExtensions.length; i++) {
			String[] splitURL = url.toString().split("\\.");
			if (splitURL.length != 0) {
				if (splitURL[splitURL.length - 1].equals(bannedExtensions[i])) {
					return false;
				}
			}
		}
		return true;
	}
	
	public void parseRobotFile() {
		try {
			currentUrl =  new URL(rootUrl, "robots.txt");
			log.debug("Checking robot file " + currentUrl.toString());
			try {
				Scanner sc = new Scanner(currentUrl.openStream());
				ArrayList<String> robots = new ArrayList<String>();
				while(sc.hasNextLine()) {
					robots.add(sc.nextLine());
				}
				
				for (int i = 0; i < robots.size(); i++) {
					String line = robots.get(i);
					String[] lineSplit = line.split(": ");
					if (lineSplit[0].equals("Allow")) {
						URL foundURL = new URL(rootUrl, lineSplit[1]);
						addToReadLater(foundURL);
						log.info("Found new URL to index: " + foundURL.toString());
					} else if (lineSplit[0].equals("Disallow")) {
						URL foundURL = new URL(rootUrl, lineSplit[1]);
						banlist.add(new BannedURL(foundURL));
					}
				}
				
			} catch (IOException e) {
				log.warn("IO Exception on parsing robots.txt - " + e.toString());
			}
		} catch (MalformedURLException e) {
			log.err(e.toString());
		}
	}
	
	public long getReadLaterLength() {
		if (mode) {
			File file = new File(System.getProperty("user.home") + "/Binocular-server/readlater.txt");
			return file.length();
		} else {
			return allowList.size();
		}
	}
	
	/**
	 * Sets how readlater should be handled.
	 * If set to true, readlater is used as the queue. This is more disk heavy, but less ram heavy.
	 * If set to false, readlater is used to archive the queue every x links. This is more ram heavy, but less disk heavy.
	 * 
	 * @param mode Should readlater be used as a queue (true), or as a queue backup (false)?
	 */
	public void setMode(boolean mode) {
		this.mode = mode;
	}
	
	private void wipeReadLater() {
		readlaterList = new ArrayList<URL>();
		File file = new File(System.getProperty("user.home") + "/Binocular-server/readlater.txt");
		file.delete();
		try {
			file.createNewFile();
		} catch (IOException e) {
			log.critical(e.toString());
		}
	}
	
	private void removeFromReadLater(URL url) {
		readlaterList.remove(url);
		File file = new File(System.getProperty("user.home") + "/Binocular-server/readlater.txt");
		try {
			List<String> out = Files.lines(file.toPath())
					.filter(line -> !line.contains(url.toString()))
					.collect(Collectors.toList());
			Files.write(file.toPath(), out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void updateBanlist() {
		long day = 86472000L;
		for (int i = 0; i < banlist.size(); i++) {
			if (banlist.get(i).getTimeBanned() + day < System.currentTimeMillis()) {
				banlist.remove(i);
			}
		}
	}
	
}
