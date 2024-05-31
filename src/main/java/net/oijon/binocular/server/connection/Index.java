package net.oijon.binocular.server.connection;

import java.io.File;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Index extends Page {

	public Index() {
		super();
		name = "index";
	}
	
	@Override
	public byte[] getContent() {
		File indexFile = new File(webdir + "/index.html");
		Document index;
		try {
			index = Jsoup.parse(indexFile);
		} catch (IOException e1) {
			log.err("Could not access page - " + e1.toString());
			index = new Document("<html><head><title>Document unavailable</title></head>\"\n"
					+ "  + \"<body><p>Document unabailable.</p></body></html>");
		}
		return index.toString().getBytes();
	}

}
