package net.oijon.binocular.server;

import java.net.URL;
import java.util.ArrayList;

public class Banlist extends ArrayList<BannedURL> {

	private static final long serialVersionUID = -8858939945023973656L;

	public boolean hasBanned(URL url) {
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).getURL().equals(url)) {
				return true;
			}
		}
		return false;
	}
	
}
