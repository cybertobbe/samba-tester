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

	    // Main route
	    from("{{int001.inbound.endpoint-uri}}")
			.routeId("int001-samba-test-main-route")
			.errorHandler(deadLetterChannelBuilder)
			
			// Message received from inbound endpoint
			.bean(TxLog.class, "info('INT001A." + loggerId + ".IN', 'Received message, exchangeId: ${exchangeId}')")
			.log(LoggingLevel.INFO, loggerId, "Incoming headers: ${headers}")
			.log(LoggingLevel.INFO, loggerId, "Incoming body: ${body}")

			// Send message to outbound endpoint
			.to("{{int001.outbound.endpoint-uri}}")
			.log(LoggingLevel.INFO, loggerId, "Outgoing headers: ${headers}")
			.bean(TxLog.class, "info('INT001B." + loggerId + ".OUT', 'Sent message to destination')")
			.log(LoggingLevel.INFO, loggerId, "Message body: ${body}");

		// Handle errors
	    from("direct:error-handler")
			.routeId("int001-samba-test-error-handler-route")
			.bean(TxLog.class, "errorEx('INT001A." + loggerId + ".ERROR')")

			// Send original file to backout folder
			.to("{{int001.backout.endpoint-uri}}")
			.bean(TxLog.class, "info('INT001A." + loggerId + ".BACKOUT', 'Backed out message')");
	}

}
