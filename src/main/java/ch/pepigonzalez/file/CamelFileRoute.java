package ch.pepigonzalez.file;

import io.smallrye.config.ConfigMapping;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.model.language.MvelExpression;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@ApplicationScoped
public class CamelFileRoute extends RouteBuilder {

    @Inject
    RouteConfig config;

    @Override
    public void configure() throws Exception {
        BindyCsvDataFormat format = new BindyCsvDataFormat(User.class);
        format.setLocale("default");


        from("file:/home/pepi/IdeaProjects/camel-file/data?maxMessagesPerPoll=1&delay=1000")
                .routeId("inRoute")
                .setHeader("fileId", constant(UUID.randomUUID().toString()) )
                .split(body().tokenize("\n")).streaming()
                .unmarshal(format)
                .setHeader("uid", new MvelExpression("request.body.id"))
                .aggregate(new MvelExpression("request.body.id"), new UserAggragationStrategy())
                    .completionTimeout(1000)
                    .id("beforeSplitter")
                    .to(config.toSplitter())
                .end();

        from(config.inSplitter())
                .multicast().parallelProcessing()
                    .to(config.toFileSingle().replace("#", "$"))
                    .to(config.toFileMulti().replace("#", "$"))
                .end();
    }
}

@ConfigMapping(prefix = "routing")
interface RouteConfig {
    String fromFile();
    String toSplitter();
    String inSplitter();
    String toFileSingle();
    String toFileMulti();
}