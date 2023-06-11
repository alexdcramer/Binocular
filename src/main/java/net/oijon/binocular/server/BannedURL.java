package net.oijon.binocular.server;

import java.net.URL;

/**
 * Allows a URL to not be scanned for a certain amount of time.
 * @author alex
 *
 */
public class BannedURL {

	private URL url;
	private long milliBanned = 0L;
	
	public BannedURL(URL url) {
		this.url = url;
		this.milliBanned = System.currentTimeMillis();
	}
	
	public long getTimeBanned() {
		return milliBanned;
	}
	
	public URL getURL() {
		return url;
	}
	
}
