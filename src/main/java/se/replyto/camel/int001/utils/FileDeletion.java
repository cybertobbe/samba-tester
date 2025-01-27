package se.replyto.camel.int001.utils;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

public class FileDeletion implements Processor {

	@Override
    public void process(Exchange exchange) throws Exception {
        // Retrieve the SMB file from the exchange
        File smbFile = exchange.getIn().getBody(File.class);

        if (smbFile != null) {
            // Retrieve the DiskShare from the exchange
            DiskShare diskShare = exchange.getProperty("CamelSmbDiskShare", DiskShare.class);

            if (diskShare != null) {
                String filePath = smbFile.getFileName();
                try {
                    // Delete the file using DiskShare
                    diskShare.rm(filePath);
                    System.out.println("File deleted successfully: " + filePath);
                } catch (Exception e) {
                    System.err.println("Failed to delete file: " + filePath);
                    throw e; // Re-throw the exception to trigger Camel's error handler
                }
            } else {
                System.err.println("DiskShare not found in the exchange.");
            }
        } else {
            System.err.println("No file found in the exchange to delete.");
        }
    }
}
