package net.oijon.binocular.server.crawler;

import java.io.IOException;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import net.oijon.binocular.server.Banlist;
import net.oijon.binocular.server.BannedURL;
import net.oijon.binocular.server.InvalidURLException;
import net.oijon.binocular.server.Main;
import net.oijon.binocular.server.Tag;
import net.oijon.utils.logger.Log;

/**
 * Allows web pages to be scraped, and queues new pages to scrape based off found links.
 * @author alex
 *
 */
public class Crawler implements Runnable {
	
	int id;
	private static Log log = Main.getLog();
	private ArrayList<URL> readLaterList = new ArrayList<URL>();
	private ArrayList<URL> coordinatedReadLater;
	private Banlist banlist;
	private boolean isScanning = false;
	
	public Crawler(int id, Banlist banlist, ArrayList<URL> coordinatedReadLater) {
		this.id = id;
		this.banlist = banlist;
		this.coordinatedReadLater = coordinatedReadLater;
	}
	
	public void parseURL(URL url) {
		long start = System.nanoTime();
		isScanning = true;
		try {
			URL newURL = sanitizeURL(url);
			log.debug("[Crawler " + id + "]" + "Sanitized URL is " + newURL);
			if (!banlist.hasBanned(newURL)) {
				parseTags(newURL);
				banlist.ban(newURL);
			}
		} catch (Exception e) {
			log.warn("[Crawler " + id + "]" + "URL '" + url + "' is invalid - " + e.toString());
			banlist.ban(url);
			e.printStackTrace();
		}
		isScanning = false;
		long end = System.nanoTime();
		long timeElapsed = end - start;
		long ms = timeElapsed / 1000000;
		log.info("[Crawler " + id + "]" + "Crawler " + id + " parsed " + url + " in " + ms + "ms.");
	}
	
	public void parseReadLater() {
		isScanning = true;
		while (readLaterList.size() > 0) {
			URL nextURL = readLaterList.get(0);
			if (!banlist.hasBanned(nextURL)) {
				parseURL(nextURL);
			}
			banlist.add(new BannedURL(nextURL));
			readLaterList.remove(0);
		}
		isScanning = false;
	}
	
	public int readLaterSize() {
		return readLaterList.size();
	}
	
	public boolean isScanning() {
		return isScanning;
	}
	
	public void queue(URL url) {
		if (isOriginalURL(url)) {
			readLaterList.add(url);
		}
	}
	
	private URL sanitizeURL(URL url) throws MalformedURLException, InvalidURLException {
		String urlString = url.toString();
		URL returnURL = url;
		if (urlIsSalvagable(url)) {
			// removes invalid characters
			
			// trailing dot
			if (urlString.charAt(urlString.length() - 1) == '.') {
				urlString = urlString.substring(0, urlString.length() - 1);
			}
			// TODO: add more
			
			
			// helpful for non-latin domains
			urlString = IDN.toASCII(urlString);
			returnURL = new URL(urlString);
		}
		
		return returnURL;
	}
	
	private boolean urlIsSalvagable(URL url) throws InvalidURLException {
		String urlString = url.toString();
		String protocol = urlString.split(":")[0];
		if (!protocol.equals("http") & !protocol.equals("https")) {
			String[] splitDot = urlString.split(".");
			boolean isIP = true;
			for (int i = 0; i < splitDot.length; i++) {
				try {
					Integer.parseInt(splitDot[i]);
				} catch (NumberFormatException e) {
					isIP = false;
					break;
				}
			}			
			if (splitDot.length != 4 | !isIP) {
				throw new InvalidURLException("Protocol '" + protocol + "' is not a scannable protocol.");
			} else {
				return true;
			}
		} else {
			return true;
		}
	}
	
	private void parseTags(URL url) throws IOException {
		log.debug("[Crawler " + id + "]" + "Parsing tags for " + url);
		Document doc = Jsoup.connect(url.toString()).get();
		parseATags(doc, url);
		parsePTags(doc, url);
	}
	
	private void parseATags(Document doc, URL url) throws IOException {
		Elements links = doc.select("a");
		int foundLinks = 0;
		for (int i = 0; i < links.size(); i++) {
			String link = links.get(i).attr("href");
			link = parseRelativeLinks(url, link);
			try {
				URL linkURL = new URL(link);
				coordinatedReadLater.add(linkURL);
				foundLinks++;
			} catch (MalformedURLException e) {
				log.warn("[Crawler " + id + "]" + "Found malformed URL " + link + " on page! - " + e.toString());
			}
		}
		log.info("[Crawler " + id + "]" + "Added " + foundLinks + " links!");
		log.debug("Total in crawler readlater: " + readLaterList.size());
		log.debug("Total in coordinated readlater: " + coordinatedReadLater.size());
	}
	
	private void parsePTags(Document doc, URL url) throws IOException {
		Elements paragraphs = doc.select("p");
		int addedTag = 0;
		for (int i = 0; i < paragraphs.size(); i++) {
			String[] splitSpace = paragraphs.get(i).text().split(" ");
			for (int j = 0; j < splitSpace.length; j++) {
				Tag tag = new Tag(splitSpace[j]);
				tag.addLink(url);
				tag.write();
				addedTag++;
			}
		}
		log.debug("[Crawler " + id + "]" + "Added " + url + " to " + addedTag + " tags!");
	}
	
	private String parseRelativeLinks(URL baseURL, String foundURL) {
		try {
			URI uri = baseURL.toURI();
			URI newLink = uri.resolve(foundURL);
			String returnString = newLink.toASCIIString();
			if (!foundURL.equals(returnString)) {
				log.debug("[Crawler " + id + "]" + "Parsed relative link from " + foundURL + ", parsed to " + returnString);
			}
			return returnString;
		} catch (URISyntaxException e) {
			log.warn("[Crawler " + id + "]" + "Invalid relative link found on page " + baseURL + "! - " + e.toString());
			return foundURL;
		}
	}
	
	private void scheduledUnbans() {
		for (int i = 0; i < banlist.size(); i++) {
			if (banlist.get(i).getTimeBanned() < System.currentTimeMillis() + 86400000L) {
				banlist.remove(i);
			}
		}
	}
	
	private boolean isOriginalURL(URL url) {
		if (readLaterList.indexOf(url) != -1) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void run() {
		parseReadLater();
		scheduledUnbans();
	}
	
	
}