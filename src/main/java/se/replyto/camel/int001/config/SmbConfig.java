//package se.replyto.camel.int001.config;
//
//
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import com.hierynomus.smbj.SMBClient;
//import com.hierynomus.smbj.auth.AuthenticationContext;
//import com.hierynomus.smbj.connection.Connection;
//import com.hierynomus.smbj.share.DiskShare;
//
//@Configuration
//	public class SmbConfig {
//
//    @Bean
//    DiskShare diskShare() throws Exception {
//	        try (SMBClient client = new SMBClient()) {
//				Connection connection = client.connect("192.168.0.158:1445");
//				AuthenticationContext authContext = new AuthenticationContext("sambauser", "password".toCharArray(), null);
//				return (DiskShare) connection.authenticate(authContext).connectShare("source");
//			}
//	    }
//	}

