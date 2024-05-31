package net.oijon.binocular.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import net.oijon.binocular.server.connection.Connection;
import net.oijon.binocular.server.crawler.CrawlerCoordinater;
import net.oijon.utils.logger.Log;

public class Main {


	static Log log = new Log(System.getProperty("user.home") + "/Binocular-server");
	static CrawlerCoordinater crawlerCoordinater = new CrawlerCoordinater(8);

	public static void init() throws URISyntaxException, IOException {
		if (new File(System.getProperty("user.home") + "/Binocular-server/web").exists() == false) {
        	new File(System.getProperty("user.home") + "/Binocular-server/web").mkdir();
        	log.info("Created web dir");
        }
		URI uri = Main.class.getResource("/web").toURI();
        Path myPath = null;
        FileSystem fileSystem = null;
        if (uri.getScheme().equals("jar")) {
	        fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
	        myPath = fileSystem.getPath("/web");
        } else {
            myPath = Paths.get(uri);
        }
        Stream<Path> walk = Files.walk(myPath, 1);
        Iterator<Path> it = walk.iterator();
        it.next();
        while (it.hasNext()){
	        try {
	        	Path filePath = it.next();
	        	String idStr = filePath.getFileName().toString();
	        	File newFile = new File(System.getProperty("user.home") + "/Binocular-server/web/" + idStr);
	        	if (!newFile.exists()) {
	        		Files.copy(filePath, new FileOutputStream(newFile));
	        		log.warn("File 'web/" + idStr + "' is missing, copying over default...");
	        	}
	        } catch (NoSuchElementException e) {
	        	log.err("Error when copying files - " + e.toString());
	        }
        }
        walk.close();
        if (fileSystem != null) {
        	fileSystem.close();
        }
	}
	
	public static void main(String[] args) {
		Connection connection = new Connection();
		connection.start();
		log.setDebug(false);
		
		log.info("Starting...");
		try {
			init();
		} catch (Exception e) {
			log.critical("Error when attempting init job - " + e.toString());
		}
		
		log.info("Started!");
		
		crawlerCoordinater.run();
	}
	
	public static Log getLog() {
		return log;
	}
	
	public static CrawlerCoordinater getCrawlerCoordinater() {
		return crawlerCoordinater;
	}
	
	
}
