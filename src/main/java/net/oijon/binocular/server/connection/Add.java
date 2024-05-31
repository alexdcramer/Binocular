package net.oijon.binocular.server.connection;

import java.io.File;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import net.oijon.binocular.server.Main;
import net.oijon.binocular.server.crawler.CrawlerCoordinater;

public class Add extends Page {

	public Add() {
		super();
		name = "add";
	}
	
	@Override
	public byte[] getContent() {
		File indexFile = new File(webdir + "/index.html");
		Document index;
		try {
			index = Jsoup.parse(indexFile);
			Element message = index.getElementById("message");
			CrawlerCoordinater crawlerCoordinater = Main.getCrawlerCoordinater();
			crawlerCoordinater.add(input);
			message.text("Successfully added '" + input + "'! This page will be indexed soon!");
		} catch (IOException e1) {
			log.err("Could not access page - " + e1.toString());
			index = new Document("<html><head><title>Document unavailable</title></head>\"\n"
					+ "  + \"<body><p>Document unavailable.</p></body></html>");
		}
		return index.toString().getBytes();
	}

}
