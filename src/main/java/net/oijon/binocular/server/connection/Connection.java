package net.oijon.binocular.server.connection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import net.oijon.binocular.server.Main;
import net.oijon.binocular.server.connection.status.StatusPage;
import net.oijon.binocular.server.crawler.CrawlerCoordinater;
import net.oijon.utils.logger.Log;

public class Connection extends Thread {

	Log log = Main.getLog();
	boolean binded = false;
	private ArrayList<Page> pages = new ArrayList<Page>();

	public Connection() {

	}

	private void init() {
		
		pages.add(new Index());
		pages.add(new Add());
		// pages.add(new Search());

	}

	public void run() {
		
		init();
		
		/*
		 * !!! ACHTUNG !!!
		 * 
		 * Before doing ANYTHING with cookies, you MUST read CVE-2023-26049.
		 * Otherwise, you WILL be sorry.
		 * 
		 * Also, read CVE-2023-26048 while you're at it.
		 */
		
		while (!binded) {
			try {
				ServerSocket serverSock = new ServerSocket(13761);
				binded = true;
				Socket sock = serverSock.accept();
				
				InputStream sis = sock.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(sis));
				String request = br.readLine(); // Now you get GET index.html HTTP/1.1
				log.info(request);
				String[] requestParam = request.split(" ");
				String path = requestParam[1];
				//path requested is expected to be weird
				//example: search?%22sahara%22;%22desert%22;
				String[] splitRequest = path.split("\\?");
				
				OutputStream os = sock.getOutputStream();
				
				boolean found = false;
				
				// overrides search and add while html frontend is WIP
				if (splitRequest[0].equals("/")) {
					splitRequest[0] = "/index";
				} else if (splitRequest[0].equals("/add")) {
					if (splitRequest.length > 1) {
						os.write(add(splitRequest[1]).getBytes());
					}
				} else if (splitRequest[9].equals("/search")) {
					if (splitRequest.length > 1) {
						os.write(search(splitRequest[1]).getBytes());
					}
				} else {
					
					for (int i = 0; i < pages.size(); i++) {
						if (splitRequest[0].equals("/" + pages.get(i).getName())) {
							found = true;
							byte[] output; 
							try {
								pages.get(i).setInput(splitRequest[1]);
							} catch (ArrayIndexOutOfBoundsException e) {
								pages.get(i).setInput("");
							}
							output = pages.get(i).run();
							os.write(output);
						}
					}
					
					
					if (!found) {
						File potentialFile = new File(Page.webdir + splitRequest[0]);
						if (potentialFile.exists() & !potentialFile.isDirectory()) {
							Page filePage = new FilePage(potentialFile);
							os.write(filePage.run());
						} else {
							Page notFound = StatusPage.NOTFOUND;
							os.write(notFound.run());
						}
					}
				}
				serverSock.close();
				binded = false;
				
			} catch (IOException e) {
				log.critical(e.toString());
			}
		}
	}
	
	private String add(String urlString) {
		String returnString = "Operation successful!";
		CrawlerCoordinater crawlerCoordinater = Main.getCrawlerCoordinater();
		crawlerCoordinater.add(urlString);
		return returnString;
	}

	private String search(String searchString) {
		String returnString = "";
		String[] terms = searchString.split(";");
		// TODO: add ability to remove search terms, require them, etc etc
		ArrayList<String> results = new ArrayList<String>();
		for (int i = 0; i < terms.length; i++) {
			String subdirs = "";
			for (int j = 0; j < terms[i].length(); j++) {
				subdirs += Character.toString(terms[i].charAt(j)) + "/";
			}
			File tagFile = new File(
					System.getProperty("user.home") + "/Binocular-server/tags/" + subdirs + terms[i] + ".btg");
			log.info(tagFile.toString());
			if (tagFile.exists()) {
				try (Scanner scan = new Scanner(tagFile)) {
					while (scan.hasNextLine()) {
						String result = scan.nextLine();
						if (!results.contains(result)) {
							results.add(result);
						}
					}
					scan.close();
				} catch (FileNotFoundException e) {
					log.err(e.toString());
				}
			}
		}
		for (int i = 0; i < terms.length; i++) {
			for (int j = 0; j < results.size(); j++) {
				String subdirs = "";
				for (int k = 0; k < terms[i].length(); k++) {
					subdirs += Character.toString(terms[i].charAt(k)) + "/";
				}
				File tagFile = new File(
						System.getProperty("user.home") + "/Binocular-server/tags/" + subdirs + terms[i] + ".btg");
				try (Scanner scan = new Scanner(tagFile)) {
					ArrayList<String> fileContents = new ArrayList<String>();
					while (scan.hasNextLine()) {
						fileContents.add(scan.nextLine());
					}
					scan.close();
					if (!fileContents.contains(results.get(j))) {
						results.remove(j);
					}
				} catch (FileNotFoundException e) {
					log.err(e.toString());
				}
			}
		}
		for (int i = 0; i < results.size(); i++) {
			returnString += results.get(i) + "\n";
		}
		if (returnString.isBlank()) {
			returnString = "No results found.";
		}
		log.info(returnString);
		return returnString;
	}

}
