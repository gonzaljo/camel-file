package ch.pepigonzalez.file;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.ArrayList;
import java.util.List;

public class UserAggragationStrategy implements AggregationStrategy {
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

        User user = newExchange.getIn().getBody(User.class);

        if (oldExchange == null) {
            ArrayList<User> users = new ArrayList<>();
            users.add(user);
            newExchange.getIn().setBody(users);
            return newExchange;
        } else {
            oldExchange.getIn().getBody(List.class).add(user);
            return oldExchange;
        }
    }
}
