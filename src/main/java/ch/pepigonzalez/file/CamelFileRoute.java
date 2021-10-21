package ch.pepigonzalez.file;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.model.language.MvelExpression;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class CamelFileRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        BindyCsvDataFormat format = new BindyCsvDataFormat(User.class);
        format.setLocale("default");

        getContext().setTracing(true);
        getContext().getTracer().setTraceBeforeAndAfterRoute(true);
        getContext().getTracer().setTracePattern("file:/mnt/d/dev/IntelliJProjects/camel-file/data");

        from("file:/mnt/d/dev/IntelliJProjects/camel-file/data?maxMessagesPerPoll=1&delay=1000")
                .setHeader("fileId", constant(UUID.randomUUID().toString()) )
                .split(body().tokenize("\n")).streaming()
                .unmarshal(format)
                .setHeader("uid", new MvelExpression("request.body.id"))
                .aggregate(new MvelExpression("request.body.id"), new UserAggragationStrategy())
                    .completionSize(100)
                    .completionTimeout(1000)
                    .to("direct:splitted")
                .end();

        from("direct:splitted")
                .multicast().parallelProcessing()
                    .to("file:/mnt/d/dev/IntelliJProjects/camel-file/data/splitted?fileName=result-${in.header.uid}")
                    .to("file:/mnt/d/dev/IntelliJProjects/camel-file/data/splitted?fileName=result-${in.header.fileId}&fileExist=Append")
                .end();
    }
}
