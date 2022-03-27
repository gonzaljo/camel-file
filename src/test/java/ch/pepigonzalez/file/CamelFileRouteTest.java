package ch.pepigonzalez.file;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.apache.camel.*;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CamelFileRouteTest {

    @EndpointInject("direct:inFile")
    ProducerTemplate fileIn;

    @EndpointInject("mock:splitter")
    ProducerTemplate mockOut;

    @EndpointInject("direct:splitterIn")
    ProducerTemplate splitterIn;

    @EndpointInject("mock:multi")
    ProducerTemplate mockMulti;

    @Inject
    CamelContext camelContext;

    @Test
    @Order(1)
    public void testLoad() throws Exception {
        AdviceWith.adviceWith(camelContext, "inRoute",
            advisor -> {
                advisor.replaceFromWith("direct:inFile");
                advisor.weaveByToUri("mock:splitter").after().to("mock:result");
            });

        //Test the mocked endpoint
        MockEndpoint ep = MockEndpoint.resolve(camelContext, "mock:splitter");
        ep.setExpectedCount(2);

        //Test the adviced Endpoint
        MockEndpoint result = MockEndpoint.resolve(camelContext, "mock:result");
        result.setExpectedCount(2);

        //Testdata
        StringBuilder builder = new StringBuilder();
        builder.append("1,1,Pepi").append("\n");
        builder.append("1,2,Manuela").append("\n");;
        builder.append("1,3,Miguel").append("\n");;
        builder.append("2,1,Roger").append("\n");;

        //Send to input
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getMessage().setBody(builder.toString());
        fileIn.send(exchange);

        //Expect 2 Messages
        assertDoesNotThrow(() -> ep.assertIsSatisfied());
        assertDoesNotThrow(() -> result.assertIsSatisfied());

        assertEquals(3, ep.getExchanges().get(0).getIn().getBody(List.class).size(), "Count of Id 1");
        assertEquals(1, ep.getExchanges().get(1).getIn().getBody(List.class).size(), "Count of Id 1");


    }

    @Test
    @Order(2)
    public void testMerge() {

        List<Exchange> exchanges = MockEndpoint.resolve(camelContext, "mock:splitter").getExchanges();

        MockEndpoint mep = MockEndpoint.resolve(camelContext, "mock:multi");
        mep.setExpectedCount(2);

        exchanges.stream().forEach((x) -> splitterIn.send(x));

        assertDoesNotThrow(() -> mep.assertIsSatisfied());
    }
}