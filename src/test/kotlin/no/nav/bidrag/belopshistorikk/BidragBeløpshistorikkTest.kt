package no.nav.bidrag.belopshistorikk

import no.nav.bidrag.belopshistorikk.BidragBeløpshistorikkTest.Companion.TEST_PROFILE
import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.ActiveProfiles

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class, ManagementWebSecurityAutoConfiguration::class])
@ActiveProfiles(TEST_PROFILE)
@ComponentScan(
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [BidragBeløpshistorikk::class, BidragBeløpshistorikkLocal::class])],
)
@EntityScan("no.nav.bidrag.belopshistorikk")
class BidragBeløpshistorikkTest {
    companion object {
        const val TEST_PROFILE = "test"
    }
}

fun main(args: Array<String>) {
    val profile = if (args.isEmpty()) TEST_PROFILE else args[0]
    val app = SpringApplication(BidragBeløpshistorikkTest::class.java)
    app.setAdditionalProfiles(profile)
    app.run(*args)
}
