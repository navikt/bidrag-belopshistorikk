package no.nav.bidrag.belopshistorikk

import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.commons.web.UserMdcFilter
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import

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
