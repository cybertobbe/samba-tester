package se.replyto.camel.int001;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import jakarta.jms.Connection;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;

/* 
 * Example usage:
 * 
 * 	    from("direct:mainRoute")
 * 			.routeId("MyRoute.mainRoute")
 *      	.bean(Logger.class, "info('int001.mainRoute', 'The flow is starting to execute')")
 *      
 */
public class TxLog {

	private static final String PATTERN_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	private String hostName = "";
	private String hostIp = "";
	private DateTimeFormatter formatter;

	public TxLog() {
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		}
		try {
			hostIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
		}
		formatter = DateTimeFormatter.ofPattern(PATTERN_FORMAT).withZone(ZoneId.of("UTC"));
	}

	private void log(LoggingLevel level, String category, String exchangeId, String message) {
		log(level, category, "[TransactionId=" + exchangeId + "] " + message);
	}

	private void log(LoggingLevel level, String category, String exchangeId, String logicalIdType, String logicalId,
			String message) {
		log(level, category, "[TransactionId=" + exchangeId + "] [LogicalIdType=" + logicalIdType + "] [LogicalId="
				+ logicalId + "] " + message);
	}

	private void logException(LoggingLevel level, String category, String exchangeId, Throwable t) {
		log(level, category, "[TransactionId=" + exchangeId + "] " + t.getMessage(), t);
	}

	private void log(LoggingLevel level, String category, String message) {

		Log logger = LogFactory.getLog(category);

		switch (level) {
		case WARN:
			logger.warn(message);
			break;
		case DEBUG:
			logger.debug(message);
			break;
		case TRACE:
			logger.trace(message);
			break;
		case ERROR:
			logger.error(message);
			break;
		case OFF:
			break;
		default:
			logger.info(message);
			break;
		}
	}

	private void log(LoggingLevel level, String category, String message, Throwable t) {

		Log logger = LogFactory.getLog(category);

		switch (level) {
		case WARN:
			logger.warn(message, t);
			break;
		case DEBUG:
			logger.debug(message, t);
			break;
		case TRACE:
			logger.trace(message, t);
			break;
		case ERROR:
			logger.error(message, t);
			break;
		case OFF:
			break;
		default:
			logger.info(message, t);
			break;
		}
	}

	public void info(String category, String message, Exchange exchange) {
		sendTxLogMessage("INFO", category, message, exchange);
		log(LoggingLevel.INFO, category, exchange.getExchangeId(), message);
	}

	public void infoWMD(String category, String logicalIdType, String logicalId, String message, Exchange exchange) {
		sendTxLogMessage("INFO", category, message, exchange, logicalIdType, logicalId);
		log(LoggingLevel.INFO, category, exchange.getExchangeId(), logicalIdType, logicalId, message);
	}

	public void warn(String category, String message, Exchange exchange) {
		sendTxLogMessage("WARN", category, message, exchange);
		log(LoggingLevel.WARN, category, exchange.getExchangeId(), message);
	}

	public void warnWMD(String category, String logicalIdType, String logicalId, String message, Exchange exchange) {
		sendTxLogMessage("WARN", category, message, exchange, logicalIdType, logicalId);
		log(LoggingLevel.WARN, category, exchange.getExchangeId(), logicalIdType, logicalId, message);
	}

	public void errorEx(String category, Exchange exchange) {
		Throwable t = exchange.getProperty("CamelExceptionCaught", Throwable.class);
		String txLogMsg = t.getMessage();
		sendTxLogMessage("ERROR", category, txLogMsg, exchange);
		logException(LoggingLevel.ERROR, category, exchange.getExchangeId(), t);
	}

	public void error(String category, String message, Exchange exchange) {
		sendTxLogMessage("ERROR", category, message, exchange);
		log(LoggingLevel.ERROR, category, exchange.getExchangeId(), message);
	}

	public void errorWMD(String category, String logicalIdType, String logicalId, String message, Exchange exchange) {
		sendTxLogMessage("ERROR", category, message, exchange, logicalIdType, logicalId);
		log(LoggingLevel.ERROR, category, exchange.getExchangeId(), logicalIdType, logicalId, message);
	}

	public void debug(String category, String message, Exchange exchange) {
		log(LoggingLevel.DEBUG, category, exchange.getExchangeId(), message);
	}

	public void debugWMD(String category, String logicalIdType, String logicalId, String message, Exchange exchange) {
		log(LoggingLevel.DEBUG, category, exchange.getExchangeId(), logicalIdType, logicalId, message);
	}

	public void trace(String category, String message, Exchange exchange) {
		log(LoggingLevel.TRACE, category, exchange.getExchangeId(), message);
	}

	public void traceWMD(String category, String logicalIdType, String logicalId, String message, Exchange exchange) {
		log(LoggingLevel.TRACE, category, exchange.getExchangeId(), logicalIdType, logicalId, message);
	}

	private void sendTxLogMessage(String loggingLevel, String category, String message, Exchange exchange) {
		sendTxLogMessage(loggingLevel, category, message, exchange, null, null);
	}

	private void sendTxLogMessage(String loggingLevel, String category, String message, Exchange exchange,
			String logicalIdType, String logicalId) {
		
		try {
			String txLogDisableActiveMqStr = exchange.getContext().resolvePropertyPlaceholders("{{txlog.disable.activemq}}");
			if(txLogDisableActiveMqStr != null && Boolean.valueOf(txLogDisableActiveMqStr)) {
				System.out.println("DEBUG: TxLog send to activemq disabled");
				return;
			}
		} catch(Exception e) {
			// Do nothing
		}
		
		Instant timestamp = Instant.now();
		Instant initialTimestamp = exchange.getProperty("baseline-initial-timestamp", Instant.class);
		if (initialTimestamp == null) {
			initialTimestamp = Instant.ofEpochMilli(exchange.getCreated());
			exchange.setProperty("baseline-initial-timestamp", initialTimestamp);
		}
		String integrationId = exchange.getContext().resolvePropertyPlaceholders("{{baseline.integration-id}}");
		String transactionId = exchange.getExchangeId().replace("-", "0");

		String contractId = integrationId;
		if (category.matches("INT[0-9]{3}[A-Z,a-z]{1,2}[.].*")) {
			contractId = category.substring(0, category.indexOf('.'));
		}

		String initialMsgid = exchange.getProperty("baseline-initial-msgid", String.class);
		if (initialMsgid == null) {
			initialMsgid = transactionId;
			exchange.setProperty("baseline-initial-msgid", initialMsgid);
		}

		String objectId = category;

//		System.out.println("Baseline log message fields");
//		System.out.println("===========================");
//		System.out.println("timestamp:        " + timestamp);
//		System.out.println("initialTimestamp: " + initialTimestamp);
//		System.out.println("integrationId:    " + integrationId);
//		System.out.println("contractId:       " + contractId);
//		System.out.println("transactionId:    " + transactionId);
//		System.out.println("initialMsgid:     " + initialMsgid);
//		System.out.println("logicalIdType:    " + logicalIdType);
//		System.out.println("logicalId:        " + logicalId);
//		System.out.println("hostName:         " + hostName);
//		System.out.println("hostIp:           " + hostIp);
//		System.out.println("level:            " + loggingLevel);
//		System.out.println("objectId:         " + objectId);
//		System.out.println("text:             " + message);

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" //
				+ "<log>\r\n" //
				+ "<timestamp>" + formatter.format(timestamp) + "</timestamp>\r\n" //
				+ "<integrationId>" + StringEscapeUtils.escapeXml10(integrationId) + "</integrationId>\r\n" //
				+ "<contractId>" + StringEscapeUtils.escapeXml10(contractId) + "</contractId>\r\n";
		if (logicalIdType != null && logicalId != null) {
			xml = xml //
					+ "<logicalIds>\r\n" //
					+ "<logicalId type=\"" + StringEscapeUtils.escapeXml10(logicalIdType) + "\">" + StringEscapeUtils.escapeXml10(logicalId) + "</logicalId>\r\n" //
					+ "</logicalIds>\r\n";
		}
		xml = xml //
				+ "<transactionId>" + StringEscapeUtils.escapeXml10(transactionId) + "</transactionId>\r\n" //
				+ "<initialMessageId>" + StringEscapeUtils.escapeXml10(initialMsgid) + "</initialMessageId>\r\n" //
				+ "<initialTimestamp>" + formatter.format(initialTimestamp) + "</initialTimestamp>\r\n" //
				+ "<hostName>" + StringEscapeUtils.escapeXml10(hostName) + "</hostName>\r\n" //
				+ "<hostIp>" + hostIp + "</hostIp>\r\n" //
				+ "<objectId>" + StringEscapeUtils.escapeXml10(objectId) + "</objectId>\r\n" //
				+ "<level>" + loggingLevel + "</level>\r\n" //
				+ "<text>" + StringEscapeUtils.escapeXml10(message) + "</text>\r\n" //
				+ "</log>";

//		System.out.println("Baseline log message:\n" + xml);
		
		sendToQueue(xml);
	}

	private void sendToQueue(String xml) {
		String brokerUrl = "tcp://localhost:61616";
		String queueName = "SAMBA";

//		System.out.println("sendToQueue: Broker URL is " + brokerUrl);
//		System.out.println("sendToQueue: Queue Name is " + queueName);

		try {
			ActiveMQConnectionFactory connectionFactory = null;
			connectionFactory = new ActiveMQConnectionFactory(brokerUrl);

			Connection connection = connectionFactory.createConnection();
			connection.start();

			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue destination = session.createQueue(queueName);
			MessageProducer producer = session.createProducer(destination);

			TextMessage message = session.createTextMessage();
			message.setText(xml);

			producer.send(message);

			producer.close();
			session.close();
			connection.close();

//			System.out.println("sendToQueue: Sent TxLog message to queue " + queueName);

		} catch (Exception e) {
			System.err.println("sendToQueue: ERROR: Failed to send TxLog message to queue " + queueName + ", Got exception " + e.getMessage());
		}
	}
}
