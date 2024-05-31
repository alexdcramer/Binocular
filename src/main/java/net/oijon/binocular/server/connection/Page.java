package net.oijon.binocular.server.connection;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.oijon.binocular.server.Main;
import net.oijon.utils.logger.Log;

public abstract class Page {

	protected static Log log = Main.getLog();
	protected static final File webdir = new File(System.getProperty("user.home") + "/Binocular-server/web");
	protected String name;
	protected String input;
	protected String HTTPver = "HTTP/1.1";
	protected String HTTPnum = "200";
	protected String HTTPstatus = "OK";
	protected String HTTPcontentType = "text/html; charset=utf-8";
	
	public Page() {
		input = "";
	}
	
	public Page(String input) {
		this.input = input;
	}
	
	public Page(Page page) {
		name = page.name;
		input = page.input;
		HTTPver = page.HTTPver;
		HTTPnum = page.HTTPnum;
		HTTPstatus = page.HTTPstatus;
		HTTPcontentType = page.HTTPcontentType;
	}
	
	public abstract byte[] getContent();
	
	public String getName() {
		return name;
	}
	
	public byte[] getHTTPHeader() {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM YYYY HH:mm:ss zzz");
		
		String header = HTTPver + " " + HTTPnum + " " + HTTPstatus + "\r\n";
		header += "Content-Type: " + HTTPcontentType + "\r\n";
		header += "Content-Length: " + getContent().length + "\r\n";
		header += "Date: " + formatter.format(date) + "\r\n";
		header += "\r\n";
		return header.getBytes();
	}
	
	public byte[] run() {
		byte[] header = getHTTPHeader();
		byte[] content = getContent();
		byte[] totalArray = new byte[header.length + content.length];
		for (int i = 0; i < header.length; i++) {
			totalArray[i] = header[i];
		}
		for (int i = 0; i < content.length; i++) {
			totalArray[i + header.length] = content[i];
		}
		return totalArray;
	}
	
	public String getInput() {
		return input;
	}
	
	public void setInput(String input) {
		this.input = input;
	}
	
}
