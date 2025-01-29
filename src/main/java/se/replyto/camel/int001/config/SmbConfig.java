package se.replyto.camel.int001.config;


import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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


@Configuration
public class SmbConfig{
	

    @Bean
    String setSambaServerAddress() {
        try {
            String hostIp = InetAddress.getLocalHost().getHostAddress();
            System.setProperty("samba.server.address", hostIp + ":1445");
            return hostIp;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "Unknown host";
        }
		
		
    }
}