package com.restkeeper.operator.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * rabbitMQ配置类
 */
@Component
public class RabbitMQConfig {

    //account distribution queue
    public static final String ACCOUNT_QUEUE = "account_queue";

    //account distribution routingkey
    public static final String ACCOUNT_QUEUE_KEY ="account_queue_key";

    //email sending exchange
    public static final String Email_EXCHANGE = "email_exchange";

    //declaration queue
    @Bean(ACCOUNT_QUEUE)
    public Queue accountQueue(){
        Queue queue = new Queue(ACCOUNT_QUEUE);
        return queue;
    }

    //declaration exchange
    @Bean(Email_EXCHANGE)
    public Exchange emailExchange(){
        return ExchangeBuilder.directExchange(Email_EXCHANGE).build();
    }

    //队列绑定交换机 queue binding exchange
    @Bean
    public Binding accountQueueToSmsExchange(@Qualifier(ACCOUNT_QUEUE) Queue queue, @Qualifier(Email_EXCHANGE) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(ACCOUNT_QUEUE_KEY).noargs();
    }
}
