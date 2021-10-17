package ch.pepigonzalez.file;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.w3c.dom.ls.LSOutput;

import java.util.List;

public class CamelFileRoute extends RouteBuilder {


    @Override
    public void configure() throws Exception {
        BindyCsvDataFormat format = new BindyCsvDataFormat(User.class);
        format.setLocale("default");

        from("file:/D:/dev/IntelliJProjects/camel-file/data?maxMessagesPerPoll=1&delay=1000")
                .split(body().tokenize("\n")).streaming()
                .unmarshal(format)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.setProperty("group", exchange.getMessage().getBody(User.class).getId());
                    }
                })
                .aggregate(header("group"), new UserAggragationStrategy())
                    .completionTimeout(5000l)
                    .to("mock:myRoute")
                .end();
    }
}
