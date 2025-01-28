package se.replyto.camel.int001.utils;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;

import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

public class FileDeletion implements Processor {

	@Override
    public void process(Exchange exchange) throws Exception {
        // Retrieve SMB file
		File smbFile = exchange.getProperty("originalFile", File.class);
		DiskShare diskShare = smbFile.getDiskShare();
		
		if (diskShare != null) {
	    	String filePath = smbFile.getPath();
	    	System.out.println("Filepath: " + filePath);
	    

	   
	    try {
	        // Delete file
	        diskShare.rm(filePath);
	        System.out.println("File deleted successfully: " + filePath);
	    } catch (Exception e) {
	    	System.out.println("Failed to delete file: " + filePath + e);
	        throw e;
	    }
		}else {
			System.out.println("Diskshare not found in exchange");
		}
    }
}
