package no.nav.bidrag.belopshistorikk

import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.nav.bidrag.belopshistorikk.hendelse.KafkaVedtakHendelseListener
import no.nav.bidrag.belopshistorikk.service.BehandleHendelseService
import no.nav.bidrag.belopshistorikk.service.JsonMapperService
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.commons.web.UserMdcFilter
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.kafka.listener.KafkaListenerErrorHandler
import org.springframework.kafka.listener.ListenerExecutionFailedException
import org.springframework.messaging.Message
import java.util.Optional

const val LIVE_PROFILE = "nais"
const val LOKAL_NAIS_PROFILE = "lokal-nais"

@Configuration
@OpenAPIDefinition(
    info = Info(title = "bidrag-beløpshistorikk", version = "v1"),
    security = [SecurityRequirement(name = "bearer-key")],
)
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
@SecurityScheme(
    bearerFormat = "JWT",
    name = "bearer-key",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
)
@EnableAspectJAutoProxy
@Import(CorrelationIdFilter::class, UserMdcFilter::class)
class BidragBeløpshistorikkConfig {
    @Bean
    fun timedAspect(registry: MeterRegistry): TimedAspect = TimedAspect(registry)
}

val LOGGER = LoggerFactory.getLogger(KafkaConfig::class.java)

@Configuration
@Profile(LIVE_PROFILE)
class KafkaConfig {
    @Bean
    fun vedtakHendelseListener(jsonMapperService: JsonMapperService, behandeHendelseService: BehandleHendelseService) =
        KafkaVedtakHendelseListener(jsonMapperService, behandeHendelseService)

    @Bean
    fun vedtakshendelseErrorHandler(): KafkaListenerErrorHandler = KafkaListenerErrorHandler {
            message: Message<*>,
            e: ListenerExecutionFailedException,
        ->
        val messagePayload: Any =
            try {
                message.payload
            } catch (re: RuntimeException) {
                "Det er ikke mulig å lese innholdet i meldingen"
            }

        LOGGER.error(
            "Feil ved behandling av hendelse, se sikker logg for detaljer: {} - {} - headers: {}",
            e.javaClass.simpleName,
            e.message,
            message.headers,
        )
        secureLogger.error {
            "Feil ved behandling av hendelse, feil ved les av hendelse: $messagePayload - exception: ${e.javaClass.simpleName} - ${e.message} " +
                "- headers: ${message.headers}"
        }
        Optional.empty<Any>()
    }
}
