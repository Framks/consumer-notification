package com.example.consumernotification.dlq

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * Guarda em memória o que caiu na DLQ, só para dar visibilidade/reprocessamento
 * via API. Simplificação de propósito: um restart do pod perde essa lista (as
 * mensagens já foram retiradas da fila real do RabbitMQ ao caírem aqui) — a
 * evolução natural disso é persistir num banco em vez de num Map.
 */
@Component
class DeadLetterStore {

    private val entries = ConcurrentHashMap<String, DeadLetterEntry>()

    fun save(entry: DeadLetterEntry) {
        entries[entry.id] = entry
    }

    fun list(): List<DeadLetterEntry> = entries.values.sortedBy { it.deadLetteredAt }

    fun remove(id: String): DeadLetterEntry? = entries.remove(id)
}
