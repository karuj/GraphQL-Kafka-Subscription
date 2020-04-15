package Kafka;

public class ConsumerUpdate {
    private final String key;
    private final String value;

    public ConsumerUpdate(String key, String value){
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
