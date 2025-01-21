package se.replyto.camel.int001.routebuilders;

import java.awt.image.renderable.ContextualRenderedImageFactory;
import java.io.IOException;
import java.io.InputStream;

import org.apache.camel.CamelContext;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.errorhandler.DefaultErrorHandlerDefinition;
import org.apache.camel.spi.TypeConvertible;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import com.hierynomus.smbj.share.File;

import se.replyto.camel.int001.TxLog;
import se.replyto.camel.int001.converter.SmbFileConverter;


@Component
public class Int001RouteBuilder extends RouteBuilder {

	private static final Logger log = LoggerFactory.getLogger(Int001RouteBuilder.class);
	String loggerId = getClass().getSimpleName();

	@Override
	public void configure() {
		
		getCamelContext().getTypeConverterRegistry().addTypeConverters(new SmbFileConverter());

		// Create error handler
	    final DefaultErrorHandlerDefinition deadLetterChannelBuilder =
	    		deadLetterChannel("direct:error-handler")
		          .useOriginalMessage()
		          .maximumRedeliveries(3)
		          .redeliveryDelay(1000);
	    
	    errorHandler(deadLetterChannelBuilder);

		    /// Main route
		    from("{{svc037.readsoftonline.inbound.fileparams}}")
		    //.process(this::process)
		    
		    .to("log:DEBUG-1?showAll=true")
		    .routeId("int001-samba-test-main-route")
		    .log(LoggingLevel.INFO, loggerId, "Starting to process files from SMB share: {{svc037.readsoftonline.inbound.fileparams}}") 
		    .log(LoggingLevel.INFO, "Incoming headers: ${headers}")
		    .setHeader("CamelFileName", simple("${headers.CamelFileName}"))
		    .setBody(exchange -> {
                File smbFile = exchange.getIn().getBody(File.class);
             
                String fileName = smbFile.getFileName(); 
                String simpleFileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
                exchange.getIn().setHeader("CamelFileName", simpleFileName);
                try (InputStream inputStream = smbFile.getInputStream()) {
                    return inputStream.readAllBytes();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
		    //.convertBodyTo(InputStream.class)
			// Send message to Samba shared folder
		    .to("log:DEBUG-2?showAll=true")
			.to("{{int001.samba.endpoint-uri}}")
			.bean(TxLog.class, "info('INT001B." + loggerId + ".OUT', 'Sent file to Samba share')");
		    
			// Handle errors
		    from("direct:error-handler")
		    .routeId("error-handler-route")
		    //.setHeader("CamelFileName", simple("file-${date:now:yyyyMMdd-HHmmss}.txt"))
		    //.setHeader("OriginalFileName", simple("${headers.camelFileName}.txt"))
		    .setHeader("CamelFileName", simple("${headers.CamelFileName}"))
		    .setBody(simple("Error occurred at ${date:now:yyyy-MM-dd HH:mm:ss}\n"
	                  + "Error Message: ${exception.message}\n"
	                  + "Stack Trace: ${exception.stacktrace}"))
		    .log(LoggingLevel.ERROR, "Error processing file: ${headers.CamelFileName}")
		    .log(LoggingLevel.ERROR, "Error processing message: ${exception.message}")
		    .log(LoggingLevel.ERROR, "Stack trace: ${exception.stacktrace}")
		    .to("{{int001.backout.endpoint-uri}}")
		    .log(LoggingLevel.INFO, "File backedout");
	}
	
//	private void process(Exchange exchange) throws IOException {
//	    final byte[] data = exchange.getMessage().getBody(byte[].class);
//	    log.debug("Read exchange as bytes with contents: {}", new String(data));
//	}
	
	private void process(Exchange exchange) throws IOException {
	    final com.hierynomus.smbj.share.File file = exchange.getMessage().getBody(com.hierynomus.smbj.share.File.class);
	    try (InputStream inputStream = file.getInputStream()) {
	        log.debug("Read exchange: {}, with contents: {}", file.getFileInformation(), new String(inputStream.readAllBytes()));
	    }
	}
	
	

}
