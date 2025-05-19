package no.nav.bidrag.belopshistorikk.hendelse

import com.fasterxml.jackson.core.JacksonException
import no.nav.bidrag.belopshistorikk.LOGGER
import no.nav.bidrag.belopshistorikk.service.BehandleHendelseService
import no.nav.bidrag.belopshistorikk.service.JsonMapperService
import no.nav.bidrag.commons.util.secureLogger
import org.springframework.kafka.annotation.KafkaListener

interface VedtakHendelseListener {
    fun lesHendelse(hendelse: String)
}

// sporingsdata fra hendelse json
open class PojoVedtakHendelseListener(
    private val jsonMapperService: JsonMapperService,
    private val behandeHendelseService: BehandleHendelseService,
) : VedtakHendelseListener {
    override fun lesHendelse(hendelse: String) {
        try {
            val vedtakHendelse = jsonMapperService.mapHendelse(hendelse)
            behandeHendelseService.behandleHendelse(vedtakHendelse)
        } catch (e: JacksonException) {
            LOGGER.error("Mapping av hendelse feilet for kafkamelding, se sikker logg for mer info")
            secureLogger.error { "Mapping av hendelse feilet for kafkamelding: $hendelse" }
            throw e
        }
    }
}

open class KafkaVedtakHendelseListener(jsonMapperService: JsonMapperService, behandeHendelseService: BehandleHendelseService) :
    PojoVedtakHendelseListener(jsonMapperService, behandeHendelseService) {
    @KafkaListener(groupId = "bidrag-belopshistorikk-1", topics = ["\${TOPIC_VEDTAK}"], errorHandler = "vedtakshendelseErrorHandler")
    override fun lesHendelse(hendelse: String) {
        super.lesHendelse(hendelse)
    }
}
