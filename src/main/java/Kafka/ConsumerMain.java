package Kafka;

public class ConsumerMain {
    public static void main(String[] args) {
        StringConsumer consumer = new StringConsumer("test123");
        while (true){
            consumer.consume();
        }
    }
}
