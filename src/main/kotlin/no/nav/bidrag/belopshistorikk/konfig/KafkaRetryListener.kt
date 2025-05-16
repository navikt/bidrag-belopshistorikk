package no.nav.bidrag.belopshistorikk.konfig

import no.nav.bidrag.commons.util.secureLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.RetryListener

class KafkaRetryListener : RetryListener {
    override fun failedDelivery(record: ConsumerRecord<*, *>, exception: Exception, deliveryAttempt: Int) {
        secureLogger.error(exception) { "Håndtering av kafkamelding ${record.value()} feilet. Dette er $deliveryAttempt. forsøk." }
    }

    override fun recovered(record: ConsumerRecord<*, *>, exception: java.lang.Exception) {
        secureLogger.error(exception) { "Håndtering av kafkamelding ${record.value()} er enten suksess eller ignorert på grunn av ugyldig data" }
    }

    override fun recoveryFailed(record: ConsumerRecord<*, *>, original: java.lang.Exception, failure: java.lang.Exception) {
    }
}
