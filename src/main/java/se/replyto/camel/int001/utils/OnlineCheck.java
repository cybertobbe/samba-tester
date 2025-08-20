package se.replyto.camel.int001.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class OnlineCheck {

	public boolean isServerOnline(String serverAddress) {
		
		final Logger log = LoggerFactory.getLogger(OnlineCheck.class);
		
		try {
			
			log.info("Checking if server is online: ", serverAddress);
			return true;
			
		}catch(Exception e){
			
			log.error("Failed to check server status ", e.getMessage());
			return false;
					
		}
	}
}
