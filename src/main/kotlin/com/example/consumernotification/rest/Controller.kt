package com.example.consumernotification.rest

import com.example.consumernotification.config.NOTIFICATION_QUEUE
import com.example.consumernotification.rest.request.NotificationRequest
import com.example.consumernotification.rest.response.NotificationResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import org.springframework.amqp.rabbit.core.RabbitTemplate

@RestController
@RequestMapping("/notification")
class Controller(
    private val rabbitTemplate: RabbitTemplate,
) {

    var logger: Logger = LoggerFactory.getLogger(NotificationRequest::class.java)

    @PostMapping
    fun add(
        @RequestBody
        notification: NotificationRequest,

    ): NotificationResponse {
        logger.info("Sending notification $notification")
        
	rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, notification)

        println("Received notification $notification")
        return NotificationResponse(
            type = notification.type,
            message = notification.message,
            readDate = LocalDateTime.now().toString(),
            createDate = LocalDateTime.now().toString(),
        )
    }
}
