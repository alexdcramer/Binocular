package net.oijon.binocular.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class Connection extends Thread {

	Log log = new Log(true);
	boolean binded = false;
	
	
	
	public Connection() {
		
	}
	
	
	public void run() {
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
				
				PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
				
				
				if (splitRequest[0].equals("/search")) {
					out.write(search(splitRequest[1]));
				}
				else if (splitRequest[0].equals("/add")) {
					out.write(add(splitRequest[1]));
				}
				out.close();
				serverSock.close();
				binded = false;
				
			} catch (IOException e) {
				log.critical(e.toString());
			}
		}
	}
	
	private String add(String urlString) {
		String returnString = "Operation successful!";
		try {
			Crawler crawler = new Crawler(new URL(urlString));
			crawler.addToReadLater(new URL(urlString));
		} catch (MalformedURLException e) {
			log.err("urlString not valid - " + e.toString());
			returnString = urlString + " is not a valid URL.";
		}
		return returnString;
	}
	
	private String search(String searchString) {
		String returnString = "";
		String[] terms = searchString.split(";");
		//TODO: add ability to remove search terms, require them, etc etc
		ArrayList<String> results = new ArrayList<String>();
		for (int i = 0; i < terms.length; i++) {
			File tagFile = new File(System.getProperty("user.home") + "/Binocular-server/tags/" + terms[i] + ".btg");
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
				File tagFile = new File(System.getProperty("user.home") + "/Binocular-server/tags/" + terms[i] + ".btg");
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
