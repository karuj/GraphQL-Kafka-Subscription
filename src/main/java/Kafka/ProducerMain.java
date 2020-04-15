package Kafka;

public class ProducerMain {
    public static void main(String[] args) throws InterruptedException {
        StringProducer producer = new StringProducer("test123");
        int i = 0;
        while (true) {
            producer.produce(i++);
            Thread.sleep(2000);
        }
    }
}
