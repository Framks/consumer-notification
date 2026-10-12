package com.example.consumernotification.dlq

import com.example.consumernotification.config.NOTIFICATION_DLQ
import com.example.consumernotification.queue.RETRY_COUNT_HEADER
import com.example.consumernotification.rest.request.NotificationRequest
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class DeadLetterConsumer(
    private val store: DeadLetterStore,
) {

    private val logger = LoggerFactory.getLogger(DeadLetterConsumer::class.java)

    @RabbitListener(queues = [NOTIFICATION_DLQ])
    fun consume(notification: NotificationRequest, message: Message) {
        val id = UUID.randomUUID().toString()
        val failedAttempts = (message.messageProperties.headers[RETRY_COUNT_HEADER] as? Int) ?: 0

        store.save(DeadLetterEntry(id, notification, failedAttempts, Instant.now()))
        logger.error("Notificação movida para a DLQ (id=$id, tentativas=$failedAttempts): $notification")
    }
}
