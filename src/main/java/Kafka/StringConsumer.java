package Kafka;

import com.google.common.collect.ImmutableMap;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.consumer.*;

import java.time.Duration;
import java.util.*;

public class StringConsumer {
    private Consumer<String, String> consumer;
    private String topic;

    public StringConsumer(String topic) {
        this.topic = topic;
        this.consumer = new KafkaConsumer(getProperties());
        consumer.subscribe(Arrays.asList(topic));
    }

    private Properties getProperties(){
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("group.id", "testgroup");
        return props;
    }
    public List<ConsumerUpdate> consume(){
        ConsumerRecords<String, String> poll = consumer.poll(1000);
        List<ConsumerUpdate> records = new ArrayList();
        poll.forEach(record -> System.out.println(record.key() + " " + record.value()));
        poll.forEach(record -> records.add(new ConsumerUpdate(record.key(), record.value())));
        return records;
    }
}
