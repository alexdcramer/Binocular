package net.oijon.binocular.server.connection.status;

import java.io.File;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.oijon.binocular.server.connection.Page;

public class StatusPage extends Page {

	protected String messageStr = "";
	
	// 1xx
	public static final StatusPage CONTINUE = new StatusPage("100", "Continue");
	public static final StatusPage SWITCHINGPROTOCOLS = new StatusPage("101", "Switching Protocols");
	public static final StatusPage PROCESSING = new StatusPage("102", "Processing");
	public static final StatusPage EARLYHINTS = new StatusPage("103", "Early Hints");
	
	// 2xx
	public static final StatusPage OK = new StatusPage("200", "OK");
	public static final StatusPage CREATED = new StatusPage("201", "Continue");
	public static final StatusPage ACCEPTED = new StatusPage("202", "Continue");
	public static final StatusPage NONAUTHORITATIVE = new StatusPage("203", "Non-Authoritative Information");
	public static final StatusPage NOCONTENT = new StatusPage("204", "No Content");
	public static final StatusPage RESETCONTENT = new StatusPage("205", "Reset Content");
	public static final StatusPage PARTIALCONTENT = new StatusPage("206", "Partial Content");
	public static final StatusPage MULTISTATUS = new StatusPage("207", "Multi-Status");
	public static final StatusPage ALREADYREPORTED = new StatusPage("208", "Already Reported");
	public static final StatusPage IMUSED = new StatusPage("226", "IM Used");
	
	// 3xx
	public static final StatusPage MULTIPLECHOICES = new StatusPage("300", "Multiple Choices");
	public static final StatusPage MOVEDPERMANENTLY = new StatusPage("301", "Moved Permanently");
	public static final StatusPage FOUND = new StatusPage("302", "Found");
	public static final StatusPage SEEOTHER = new StatusPage("303", "See Other");
	public static final StatusPage NOTMODIFIED = new StatusPage("304", "Not Modified");
	public static final StatusPage TEMPORARYREDIRECT = new StatusPage("307", "Temporary Redirect");
	public static final StatusPage PERMANENTREDIRECT = new StatusPage("308", "Permanent Redirect");
	
	// 4xx
	public static final StatusPage BADREQUEST = new StatusPage("400", "Bad Request");
	public static final StatusPage UNAUTHORIZED = new StatusPage("401", "Unauthorized");
	public static final StatusPage PAYMENTREQUIRED = new StatusPage("402", "Payment Required");
	public static final StatusPage FORBIDDEN = new StatusPage("403", "Forbidden");
	public static final StatusPage NOTFOUND = new StatusPage("404", "Not Found");
	
	// 5xx
	
	private StatusPage(String num, String msg) {
		super();
		name = num;
		HTTPnum = num;
		HTTPstatus = msg;
	}
	
	public byte[] getContent() {
		File indexFile = new File(webdir + "/status.html");
		Document index;
		try {
			index = Jsoup.parse(indexFile);
			Elements status = index.getElementsByClass("status");
			for (int i = 0; i < status.size(); i++) {
				status.get(i).text(HTTPnum + " " + HTTPstatus);
			}
			Element message = index.getElementById("message");
			message.text(messageStr);
		} catch (IOException e1) {
			log.err("Could not access page - " + e1.toString());
			index = new Document("<html><head><title>Document unavailable</title></head>\"\n"
					+ "  + \"<body><p>Document unabailable.</p></body></html>");
		}
		return index.toString().getBytes();
	}
	
	
}
