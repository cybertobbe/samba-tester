package se.replyto.camel.int001.utils;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import se.replyto.camel.int001.routebuilders.Int001RouteBuilder;

public class CheckIfFileExists {

	private static final Logger logger = LoggerFactory.getLogger(Int001RouteBuilder.class);

	
	private boolean checkIfFileExists(Exchange exchange, String fileName) {
		String destinationUri = exchange.getContext().resolvePropertyPlaceholders("{{int001.samba.endpoint-uri}}");
	    String sharePath = destinationUri.substring(destinationUri.lastIndexOf("/") + 1).split("\\?")[0];
	    logger.info("Path to share: ", sharePath);
	    SMBClient client = new SMBClient();
	    try (Connection connection = client.connect("192.168.0.158")) {
	        AuthenticationContext authContext = new AuthenticationContext("sambauser", "password".toCharArray(), null);
	        Session session = connection.authenticate(authContext);

	        try (DiskShare share = (DiskShare) session.connectShare(sharePath)) {
	            // Check if the file exists
	            return share.fileExists(fileName);
	        }
	    } catch (IOException e) {
	        System.out.println("Error checking if file '{}' exists: {}" + fileName + e.getMessage());
	        return false;
	    }
	}
}
