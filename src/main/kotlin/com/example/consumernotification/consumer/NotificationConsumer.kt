package com.example.consumernotification.queue

import com.example.consumernotification.config.NOTIFICATION_QUEUE
import com.example.consumernotification.rest.request.NotificationRequest
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

const val RETRY_COUNT_HEADER = "x-retry-count"

@Component
class NotificationConsumer(
    private val provider: NotificationProviderSimulator,
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${notification.max-retries:3}") private val maxRetries: Int,
) {

    private val logger = LoggerFactory.getLogger(NotificationConsumer::class.java)

    @RabbitListener(queues = [NOTIFICATION_QUEUE])
    fun consume(notification: NotificationRequest, message: Message) {
        val retryCount = (message.messageProperties.headers[RETRY_COUNT_HEADER] as? Int) ?: 0

        try {
            provider.send(notification, retryCount)
            logger.info("Notificação entregue: $notification")
        } catch (ex: NotificationDeliveryException) {
            if (retryCount < maxRetries) {
                logger.warn("Falha ao entregar (tentativa ${retryCount + 1}/$maxRetries): $notification")
                rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, notification) { msg ->
                    msg.messageProperties.headers[RETRY_COUNT_HEADER] = retryCount + 1
                    msg
                }
            } else {
                logger.error("Falha definitiva após $maxRetries tentativas, indo para a DLQ: $notification")
                throw ex
            }
        }
    }
}
