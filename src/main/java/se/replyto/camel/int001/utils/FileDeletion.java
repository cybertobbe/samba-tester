package se.replyto.camel.int001.utils;



import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

public class FileDeletion implements Processor {


	
	@Override
    public void process(Exchange exchange) throws Exception {
		
		final Logger log = LoggerFactory.getLogger(FileDeletion.class);

		
        File smbFile = exchange.getProperty("originalFile", File.class);
        if (smbFile != null) {
            String filePath = smbFile.getPath();
            boolean deleted = false;
            

            while (!deleted) {
                try {
                    // Close session
                    smbFile.close();
                    
                    // Try to delete the file
                    DiskShare share = (DiskShare) smbFile.getDiskShare();
                    share.rm(filePath);
                    deleted = true;
                } catch (SMBApiException e) {
                    if (e.getStatusCode() == 0xc0000043) {
                        log.warn("STATUS_SHARING_VIOLATION: Retry to delete file: ", filePath);

                    } else {
                        throw e;
                    }
                }
            }

            if (!deleted) {
                log.error("Failed to delete file: ", filePath);
                throw new RuntimeException("Failed to delete file: " + filePath);
            }
        }
    }
}
