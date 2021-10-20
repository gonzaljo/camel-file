package ch.pepigonzalez.file;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.model.language.MvelExpression;

import javax.inject.Inject;

public class CamelFileRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        BindyCsvDataFormat format = new BindyCsvDataFormat(User.class);
        format.setLocale("default");

        getContext().setTracing(true);
        getContext().getTracer().setTraceBeforeAndAfterRoute(true);
        getContext().getTracer().setTracePattern("file:/mnt/d/dev/IntelliJProjects/camel-file/data");

        from("file:/mnt/d/dev/IntelliJProjects/camel-file/data?maxMessagesPerPoll=1&delay=1000")
                .split(body().tokenize("\n")).streaming()
                .unmarshal(format)
                .aggregate(new MvelExpression("request.body.id"), new UserAggragationStrategy())
                    .completionTimeout(1l)
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            System.out.println("Body size: " + exchange.getIn().getBody().toString());
                        }
                    })
                    .to("mock:myRoute")
                .end();
    }
}
