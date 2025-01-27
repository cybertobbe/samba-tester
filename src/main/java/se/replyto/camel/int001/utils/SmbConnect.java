//package se.replyto.camel.int001.utils;
//
//import java.io.IOException;
//
//import org.springframework.stereotype.Component;
//
//import com.hierynomus.smbj.SMBClient;
//import com.hierynomus.smbj.auth.AuthenticationContext;
//import com.hierynomus.smbj.connection.Connection;
//import com.hierynomus.smbj.session.Session;
//import com.hierynomus.smbj.share.DiskShare;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//
//@Component
//public class SmbConnect {
//
//    private SMBClient smbClient;
//    private Connection connection;
//    private Session session;
//    private DiskShare share;
//
//    @PostConstruct
//    public void init() throws IOException {
//        smbClient = new SMBClient();
//        connection = smbClient.connect("192.168.0.158");
//        AuthenticationContext auth = new AuthenticationContext("sambauser", "password".toCharArray(), null);
//        session = connection.authenticate(auth);
//        share = (DiskShare) session.connectShare("source");
//    }
//
//    @PreDestroy
//    public void cleanup() throws IOException {
//        if (share != null) share.close();
//        if (session != null) session.close();
//        if (connection != null) connection.close();
//        if (smbClient != null) smbClient.close();
//    }
//
//    public DiskShare getShare() {
//        return share;
//    }
//}