//package se.replyto.camel.int001.config;
//
//import org.apache.camel.CamelContext;
//import org.apache.camel.impl.DefaultCamelContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class CamelConfig {
//
//    @Bean
//    CamelContext camelContext() throws Exception {
//        CamelContext camelContext = new DefaultCamelContext();
//        camelContext.getTypeConverterRegistry().addTypeConverters(new se.replyto.camel.int001.converter.SmbFileConverter());
//        return camelContext;
//    }
//}
