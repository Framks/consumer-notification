package com.example.consumernotification.dlq

import com.example.consumernotification.rest.request.NotificationRequest
import java.time.Instant

data class DeadLetterEntry(
    val id: String,
    val notification: NotificationRequest,
    val failedAttempts: Int,
    val deadLetteredAt: Instant,
)
