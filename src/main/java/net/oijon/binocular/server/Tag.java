package net.oijon.binocular.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class Tag {

	File tagFile;
	ArrayList<URL> linkList = new ArrayList<URL>();
	Log log = new Log(true);
	String name;
	
	
	public Tag(String name) {
		this.name = name;
		tagFile = new File(System.getProperty("user.home") + "/Binocular-server/tags/" + name + ".btg");
		String fileString = tagFile.toString();
		if (fileString.length() < 256) {
			if (tagFile.exists()) {
				parseTag(tagFile);
			} else {
				tagFile.getParentFile().mkdirs();
				try {
					tagFile.createNewFile();
				} catch (IOException e) {
					log.err(e.toString());
				}
			}
		}
	}
	
	public void addLink(URL url) {
		String fileString = url.toString();
		if (fileString.length() < 256) {
			if (isIn(url) == false) {
				linkList.add(url);
			}
		write();
		}
	}
	
	public void parseTag(File file) {
		String fileString = file.toString();
		if (fileString.length() < 256) {
			try {
				Scanner sc = new Scanner(file);
				while (sc.hasNextLine()) {
					String urlString = sc.nextLine();
					if (!urlString.isBlank()) {
						try {
							URL currentURL = new URL(urlString);
							linkList.add(currentURL);
						} catch (MalformedURLException e) {
							log.err(e.toString() + " - " + urlString);
						}
					}
				}
				sc.close();
			} catch (FileNotFoundException e) {
				log.err(e.toString() + " - " + fileString);
			}
		}
		
	}
	
	public void write() {
		String fileString = tagFile.toString();
		if (fileString.length() < 256) {
			try {
				tagFile.getParentFile().mkdirs();
				FileWriter fw = new FileWriter(tagFile, false);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(toString());
				bw.newLine();
			    bw.close();
			} catch (IOException e) {
				log.err(e.toString());
			}
		}
	}
	
	public boolean isIn(URL url) {
		String urlString = tagFile.toString();
		if (urlString.length() < 256) {
			try (Scanner sc = new Scanner(tagFile)) {
				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					if (line.equals(url.toString())) {
						return true;
					}
				}
				sc.close();
				return false;
			} catch (FileNotFoundException e) {
				log.err(e.toString());
				return false;
			}
		} else {
			return false;
		}
	}
	
	public String toString() {
		String returnString = "";
		for (int i = 0; i < linkList.size(); i++) {
			if (!linkList.get(i).toString().isBlank()) {
				returnString += linkList.get(i).toString() + "\n"; 
			}
		}
		return returnString;
	}
}
