package net.oijon.binocular.server.connection;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

public class FilePage extends Page {

	private File file;
	
	public FilePage() {
		super();
		name = "binocular-plain-file";
		this.file = new File(webdir + "index.html");
	}
	
	public FilePage(File file) {
		super();
		name = "binocular-plain-file";
		this.file = file;
		findContentType();
	}
	
	@Override
	public byte[] getContent() {
		try {
			return FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			log.err("Could not read file - " + e.toString());
			return new byte[0];
		}
	}
	
	private void findContentType() {
		HashMap<String, String> mimes = new HashMap<String, String>();
		
		mimes.put("png", "image/png");
		mimes.put("css", "text/css");
		
		
		HTTPcontentType = "application/octet-stream";
		String fileName = file.getName();
	    int dotIndex = fileName.lastIndexOf('.');
	    String extension = (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
	    if (mimes.get(extension) != null) {
	    	HTTPcontentType = mimes.get(extension);
	    }
	}

}
