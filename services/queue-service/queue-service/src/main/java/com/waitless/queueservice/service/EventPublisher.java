package com.waitless.queueservice.service;


import com.waitless.queueservice.entity.Counter;
import com.waitless.queueservice.entity.Queue;
import com.waitless.queueservice.event.CounterEvent;
import com.waitless.queueservice.event.QueueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    public final KafkaTemplate<String,Object> kafkaTemplate;
    public static final String QUEUE_EVENTs_TOPIC = "queue_events";
    public static final String COUNTER_EVENTS_TOPIC = "counter_events";


    //Queues events :

    public void  publishQueueCreated( Queue queue){

        QueueEvent event = QueueEvent.builder()
                .eventType("Queue_Created")
                .queueId(queue.getId())
                .companyId(queue.getCompany().getId())
                .queueName(queue.getName())
                .isActive(true)
                .capacity(queue.getCapacity())
                .averageServiceTime(queue.getAverageServiceTime())
                .timestamp(LocalDateTime.now())
                .build();

           publishEvent(QUEUE_EVENTs_TOPIC,queue.getId().toString(),event);

        log.info(" Published QUEUE_CREATED event for queue: {}", queue.getId());

    }

    public void  publishQueueOpened( Queue queue){

        QueueEvent event = QueueEvent.builder()
                .eventType("Queue_Opened")
                .queueId(queue.getId())
                .companyId(queue.getCompany().getId())
                .queueName(queue.getName())
                .isActive(true)
                .timestamp(LocalDateTime.now())
                .build();

        publishEvent(QUEUE_EVENTs_TOPIC,queue.getId().toString(),event);

        log.info(" Published QUEUE_OPENED event for queue: {}", queue.getId());

    }
    public void  publishQueueClosed( Queue queue){

        QueueEvent event = QueueEvent.builder()
                .eventType("Queue_Closed")
                .queueId(queue.getId())
                .companyId(queue.getCompany().getId())
                .queueName(queue.getName())
                .isActive(false)
                .timestamp(LocalDateTime.now())
                .build();

        publishEvent(QUEUE_EVENTs_TOPIC,queue.getId().toString(),event);

        log.info(" Published QUEUE_CLOSED event for queue: {}", queue.getId());

    }

    //Counters events

    public void publishCounterOpened(Counter counter) {
        CounterEvent event = CounterEvent.builder()
                .eventType("COUNTER_OPENED")
                .counterId(counter.getId())
                .queueId(counter.getQueue().getId())
                .counterNumber(counter.getCounterNumber())
                .isActive(true)
                .timestamp(LocalDateTime.now())
                .build();

        publishEvent(COUNTER_EVENTS_TOPIC, counter.getId().toString(), event);
        log.info("Published COUNTER_OPENED event for counter: {}", counter.getId());
    }

    public void publishCounterClosed(Counter counter) {
        CounterEvent event = CounterEvent.builder()
                .eventType("COUNTER_CLOSED")
                .counterId(counter.getId())
                .queueId(counter.getQueue().getId())
                .counterNumber(counter.getCounterNumber())
                .isActive(false)
                .timestamp(LocalDateTime.now())
                .build();

        publishEvent(COUNTER_EVENTS_TOPIC, counter.getId().toString(), event);
        log.info("Published COUNTER_CLOSED event for counter: {}", counter.getId());
    }

    //publishEvent

    private void publishEvent(String topic , String key , Object event) {

        try{

            kafkaTemplate.send(topic, key,event);
            log.debug(" Event published to topic: {} with key: {}", topic, key);

        }catch (Exception e){
          log.debug("ailed to publish event to topic: {}", topic, e);
        }
    }

}
