package no.nav.bidrag.belopshistorikk

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Profile

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class, ManagementWebSecurityAutoConfiguration::class])
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
@ComponentScan(excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [BidragBeløpshistorikk::class])])
@Profile("lokal-nais")
class BidragBeløpshistorikkLokalNais

fun main(args: Array<String>) {
    val app = SpringApplication(BidragBeløpshistorikkLokalNais::class.java)
    app.setAdditionalProfiles("lokal-nais", "lokal-nais-secrets")
    app.run(*args)
}
