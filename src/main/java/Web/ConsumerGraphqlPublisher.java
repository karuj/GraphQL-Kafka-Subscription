package Web;

import Kafka.ConsumerPublisher;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

public class ConsumerGraphqlPublisher {
    private final static ConsumerPublisher CONSUMER_PUBLISHER = new ConsumerPublisher();

    private final GraphQLSchema graphQLSchema;

    public ConsumerGraphqlPublisher(){graphQLSchema = buildSchema();}

    private GraphQLSchema buildSchema() {
        Reader streamReader = loadSchemaFile("consumer.graphqls");
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(streamReader);

        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Subscription")
                        .dataFetcher("records", consumerSubscriptionFetcher()))
                .build();
        return new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
    }

    private DataFetcher consumerSubscriptionFetcher() {
        System.out.println("in resolver<<<<<<<<");
        return environment -> {
            return CONSUMER_PUBLISHER.getPublisher();
        };
    }

    public GraphQLSchema getGraphQLSchema() {return graphQLSchema;}

    private Reader loadSchemaFile(String name){
        InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
        return new InputStreamReader(stream);
    }
}
