package com.example.consumernotification.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val NOTIFICATION_QUEUE = "queue.notification"
const val NOTIFICATION_DLQ = "queue.notification.dlq"
const val NOTIFICATION_DLX = "notification.dlx"

@Configuration
class RabbitMQConfig {

    @Bean
    fun notificationQueue(): Queue =
        QueueBuilder.durable(NOTIFICATION_QUEUE)
            .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
            .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ)
            .build()

    @Bean
    fun notificationDlq(): Queue = QueueBuilder.durable(NOTIFICATION_DLQ).build()

    @Bean
    fun notificationDlx(): DirectExchange = DirectExchange(NOTIFICATION_DLX)

    @Bean
    fun dlqBinding(notificationDlq: Queue, notificationDlx: DirectExchange): Binding =
        BindingBuilder.bind(notificationDlq).to(notificationDlx).with(NOTIFICATION_DLQ)

    @Bean
    fun jsonMessageConverter(): MessageConverter = Jackson2JsonMessageConverter()
}
