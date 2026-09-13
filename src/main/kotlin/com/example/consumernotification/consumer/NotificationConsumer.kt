package com.example.consumernotification.queue

import com.example.consumernotification.rest.request.NotificationRequest
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class NotificationConsumer {

    private val logger = LoggerFactory.getLogger(NotificationConsumer::class.java)

    @RabbitListener(queues = ["queue.notification"])
    fun consume(notification: NotificationRequest) {
        logger.info("Consumindo notificação da fila: $notification")
    }
}
