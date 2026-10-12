package com.example.consumernotification.queue

import com.example.consumernotification.rest.request.NotificationRequest
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * Simula um provedor externo de envio (SMTP, SMS gateway, etc.). Além de uma
 * falha aleatória residual, aceita gatilhos explícitos no campo `message` pra
 * dar controle determinístico ao testar retry/DLQ na mão:
 *   - "FAIL_PERMANENT": falha sempre (demonstra chegada garantida na DLQ)
 *   - "FAIL_ONCE": falha só na primeira tentativa (demonstra retry recuperando)
 */
@Component
class NotificationProviderSimulator {

    private val randomFailureRate = 0.1

    fun send(notification: NotificationRequest, retryCount: Int) {
        val message = notification.message

        val shouldFail = when {
            message.contains("FAIL_PERMANENT", ignoreCase = true) -> true
            message.contains("FAIL_ONCE", ignoreCase = true) -> retryCount == 0
            else -> Random.nextDouble() < randomFailureRate
        }

        if (shouldFail) {
            throw NotificationDeliveryException("Falha simulada ao enviar via provedor externo: $notification")
        }
    }
}
