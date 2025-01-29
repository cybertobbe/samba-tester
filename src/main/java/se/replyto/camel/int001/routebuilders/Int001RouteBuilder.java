package se.replyto.camel.int001.routebuilders;

import java.io.IOException;
import java.io.InputStream;


import org.apache.camel.LoggingLevel;

import org.apache.camel.builder.RouteBuilder;

import org.apache.camel.model.errorhandler.DefaultErrorHandlerDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hierynomus.smbj.share.File;
import se.replyto.camel.int001.TxLog;
import se.replyto.camel.int001.utils.FileDeletion;


@Component
public class Int001RouteBuilder extends RouteBuilder {

	private static final Logger log = LoggerFactory.getLogger(Int001RouteBuilder.class);
	String loggerId = getClass().getSimpleName();

	
	
	@Override
	public void configure() {
		
		

		// Create error handler
	    final DefaultErrorHandlerDefinition deadLetterChannelBuilder =
	    		deadLetterChannel("direct:error-handler")
		          .useOriginalMessage()
		          .maximumRedeliveries(3)
		          .redeliveryDelay(1000);
	    
	    errorHandler(deadLetterChannelBuilder);
	    
	    

		  // Main route
		  from("{{int001.inbound.files-uri}}")
	    	.log(LoggingLevel.INFO, loggerId, "Quartz start, polling...")
		    .to("log:DEBUG-1?showAll=true")
		    .routeId("int001-samba-test-main-route")
		    
		    .log(LoggingLevel.INFO, loggerId, "Starting to process files from SMB share: {{int001.inbound.files-uri}}") 
		    .log(LoggingLevel.INFO, "Incoming headers: ${headers}")
		    
		    
		    .setBody(exchange -> {
		    	
		    	
		    	
                File smbFile = exchange.getIn().getBody(File.class);
                exchange.setProperty("originalFile", smbFile);
                
                String fileName = smbFile.getPath(); 
                String simpleFileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
                exchange.getIn().setHeader("CamelFileName", simpleFileName);
                
                log.info("Checking if file exists in the destination folder", simpleFileName);
                
                
                
                try (InputStream inputStream = smbFile.getInputStream()) {
                    return inputStream.readAllBytes();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                
            })
		    
		    //.filter(simple("${header.CamelFileName} regex '^.*\\.txt$'")) 
		    .filter(header("CamelFileName").regex(".*\\.(txt|xml)$"))
		    
			// Send message to Samba shared folder
		    .to("log:DEBUG-2?showAll=true")
			.to("{{int001.samba.endpoint-uri}}")
			.process(new FileDeletion())
			.bean(TxLog.class, "info('INT001B." + loggerId + ".OUT', 'Sent file to Samba share')");
		    	
		    
			
		    
			// Handle errors
		    from("direct:error-handler")
		    .routeId("error-handler-route")
		    .setHeader("CamelFileName", simple("${headers.CamelFileName}"))
		    .log(LoggingLevel.ERROR, "Error processing file: ${headers.CamelFileName}")
		    .log(LoggingLevel.ERROR, "Error processing message: ${exception.message}")
		    .log(LoggingLevel.ERROR, "Stack trace: ${exception.stacktrace}")
		    .to("{{int001.backout.endpoint-uri}}")
		    .log(LoggingLevel.INFO, "File backedout");
	        
	    }
	
	
	}
