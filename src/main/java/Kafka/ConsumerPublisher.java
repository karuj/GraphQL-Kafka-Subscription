package Kafka;


import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.observables.ConnectableObservable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConsumerPublisher {

    private final Flowable<ConsumerUpdate> publisher;
    private final StringConsumer consumer = new StringConsumer("test123");

    public ConsumerPublisher() {
        Observable<ConsumerUpdate> consumerUpdateObservable = Observable.create(emitter -> {
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
            executorService.scheduleAtFixedRate(newConsume(emitter), 0, 2, TimeUnit.SECONDS);
        });
        ConnectableObservable<ConsumerUpdate> connectableObservable = consumerUpdateObservable.share().publish();
        connectableObservable.connect();

        publisher = connectableObservable.toFlowable(BackpressureStrategy.BUFFER);
    }

    private Runnable newConsume(ObservableEmitter<ConsumerUpdate> emitter){
        return () -> {
            List<ConsumerUpdate> consumerUpdates = getUpdates();
            if (consumerUpdates.size() != 0) emitConsumes(emitter, consumerUpdates);
        };
    }

    private void emitConsumes(ObservableEmitter<ConsumerUpdate> emitter, List<ConsumerUpdate> consumerUpdates){
        for (ConsumerUpdate consumerUpdate : consumerUpdates){
            try {
                emitter.onNext(consumerUpdate);
            } catch (RuntimeException e){
                e.printStackTrace();
            }
        }
    }

    public Flowable<ConsumerUpdate> getPublisher(){return publisher;}

    private List<ConsumerUpdate> getUpdates(){
        List<ConsumerUpdate> updates = consumer.consume();
        return updates;
    }


}
