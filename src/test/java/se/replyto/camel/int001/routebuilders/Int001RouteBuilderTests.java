package se.replyto.camel.int001.routebuilders;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@CamelSpringBootTest
@EnableAutoConfiguration
@SpringBootTest( properties = {"spring.boot.admin.client.enabled=false"} )
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@UseAdviceWith
@Disabled
class Int001RouteBuilderTestCase  {

    @Autowired
    private CamelContext context;

    @Produce("direct:start")
    protected ProducerTemplate template;
	
    @EndpointInject("mock:out")
    MockEndpoint mockOutEndpoint;    

    @EndpointInject("mock:backout")
    MockEndpoint mockBackoutEndpoint;    

    @BeforeEach
	public void initialize() {
		MockEndpoint.resetMocks(context);
	}

	@Test
	public void contextLoads() {
	}
    
    @Test
    public void int001TestHappyPath() throws Exception {

    	mockOutEndpoint.expectedMessageCount(1);
    	mockBackoutEndpoint.expectedMessageCount(0);
    	mockOutEndpoint.expectedBodyReceived().constant("Test data.");

        context.start();
        
        template.sendBody("Test data.");
    	
    	MockEndpoint.assertIsSatisfied(context);
    }

    @Test
    public void int001TestError() throws Exception {
        AdviceWith.adviceWith(context, "int001-samba-test-main-route", a -> {
        		a.weaveByToUri("*").before().throwException(new RuntimeException("Error sending to destination!"));
        	}
        );

    	mockOutEndpoint.expectedMessageCount(0);
    	mockBackoutEndpoint.expectedMessageCount(1);
    	mockBackoutEndpoint.expectedBodyReceived().constant("Test data.");

        context.start();
        
        template.sendBody("Test data.");
    	
    	MockEndpoint.assertIsSatisfied(context);
    }
}
