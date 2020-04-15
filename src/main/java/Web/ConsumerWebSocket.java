package Web;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.JsonKit;
import util.QueryParameters;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class ConsumerWebSocket extends WebSocketAdapter {
    private static final Logger log = LoggerFactory.getLogger(ConsumerWebSocket.class);

    private final ConsumerGraphqlPublisher graphqlPublisher = new ConsumerGraphqlPublisher();
    private final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();

    public void onWebsocketConnect(Session session) {super.onWebSocketConnect(session);}

    public void onWebSocketClose(int statusCode, String reason){
        log.info("Closing web socket");
        super.onWebSocketClose(statusCode, reason);
        Subscription subscription = subscriptionRef.get();
        if (subscription != null)
            subscription.cancel();
    }

    public void onWebSocketText(String graphqlQuery){
        log.info("Websocket said {}", graphqlQuery);
        QueryParameters parameters = QueryParameters.from(graphqlQuery);

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(parameters.getQuery())
                .variables(parameters.getVariables())
                .operationName(parameters.getOperationName())
                .build();

        Instrumentation instrumentation = new ChainedInstrumentation(
                Collections.singletonList(new TracingInstrumentation())
        );

        GraphQL graphQL = GraphQL
                .newGraphQL(graphqlPublisher.getGraphQLSchema())
                .instrumentation(instrumentation)
                .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        Publisher<ExecutionResult> consumerStream = executionResult.getData();

        consumerStream.subscribe(new Subscriber<ExecutionResult>() {
            @Override
            public void onSubscribe(Subscription s) {
                subscriptionRef.set(s);
                request(1);
            }

            @Override
            public void onNext(ExecutionResult er) {
                log.debug("Sending consumer update");
                try {
                    Object consumerUpdate = er.getData();
                    getRemote().sendString(JsonKit.toJsonString(consumerUpdate));
                } catch (IOException e){
                    e.printStackTrace();
                }
                request(1);
            }

            @Override
            public void onError(Throwable t) {
                log.error("Subscription threw an exception", t);
                getSession().close();
            }

            @Override
            public void onComplete() {
                log.info("Subscription complete");
                getSession().close();
            }
        });
    }

    private void request(int n){
        Subscription subscription = subscriptionRef.get();
        if (subscription != null)
            subscription.request(n);
    }
}
