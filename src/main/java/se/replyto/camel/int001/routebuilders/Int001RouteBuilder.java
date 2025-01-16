package se.replyto.camel.int001.routebuilders;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.errorhandler.DefaultErrorHandlerDefinition;
import org.springframework.stereotype.Component;

import se.replyto.camel.int001.TxLog;

@Component
public class Int001RouteBuilder extends RouteBuilder {

	String loggerId = getClass().getSimpleName();

	@Override
	public void configure() {

		// Create error handler
	    final DefaultErrorHandlerDefinition deadLetterChannelBuilder =
	    		deadLetterChannel("direct:error-handler")
		          .useOriginalMessage()
		          .maximumRedeliveries(0)
		          .redeliveryDelay(1000);
	    
	    errorHandler(noErrorHandler());

		    /// Main route
		    from("{{int001.inbound.endpoint-uri}}")
		    .routeId("int001-samba-test-main-route")
		    .errorHandler(deadLetterChannel("direct:error-handler")
		        .maximumRedeliveries(3)
		        .redeliveryDelay(1000))
		    .log(LoggingLevel.INFO, "Incoming headers: ${headers}")
		    .log(LoggingLevel.INFO, "Incoming body: ${body}")

			// Send message to Samba shared folder
			.to("{{int001.samba.endpoint-uri}}")
			.log(LoggingLevel.INFO, loggerId, "File sent to Samba share: ${headers.CamelFileName}")
			.bean(TxLog.class, "info('INT001B." + loggerId + ".OUT', 'Sent file to Samba share')");

			// Handle errors
		    from("direct:error-handler")
		    .routeId("error-handler-route")
		    .log(LoggingLevel.ERROR, "Error processing message: ${exception.message}")
		    .to("{{int001.backout.endpoint-uri}}");
	}

}
