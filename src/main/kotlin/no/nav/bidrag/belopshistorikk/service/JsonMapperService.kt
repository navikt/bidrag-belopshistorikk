package no.nav.bidrag.belopshistorikk.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.transport.behandling.vedtak.VedtakHendelse
import org.springframework.stereotype.Service

@Service
class JsonMapperService(private val objectMapper: ObjectMapper) {
    fun mapHendelse(hendelse: String): VedtakHendelse = try {
        objectMapper.readValue(hendelse, VedtakHendelse::class.java)
    } finally {
        secureLogger.debug { "Leser hendelse: $hendelse" }
    }

    fun readTree(hendelse: String) = objectMapper.readTree(hendelse)
}
