package com.example.consumernotification.dlq

import com.example.consumernotification.config.NOTIFICATION_QUEUE
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/dlq")
class DeadLetterController(
    private val store: DeadLetterStore,
    private val rabbitTemplate: RabbitTemplate,
) {

    @GetMapping
    fun list(): List<DeadLetterEntry> = store.list()

    @PostMapping("/{id}/retry")
    fun retry(@PathVariable id: String): ResponseEntity<Void> {
        val entry = store.remove(id) ?: return ResponseEntity.notFound().build()
        rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, entry.notification)
        return ResponseEntity.ok().build()
    }
}
