package Kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class StringProducer {
    private Producer<String, String> producer;
    private String topic;

    public StringProducer(String topic) {
        this.producer = new KafkaProducer<String, String>(getProperties());
        this.topic = topic;
    }

    private Properties getProperties(){
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        return props;
    }
    public void produce(int i){
        ProducerRecord<String, String> record = new ProducerRecord(topic, String.valueOf(i), "test");
        producer.send(record);
    }
}
