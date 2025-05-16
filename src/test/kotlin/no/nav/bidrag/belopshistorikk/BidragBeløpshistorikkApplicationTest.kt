package no.nav.bidrag.belopshistorikk

import no.nav.bidrag.belopshistorikk.BidragBeløpshistorikkTest.Companion.TEST_PROFILE
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = [BidragBeløpshistorikkTest::class])
@ActiveProfiles(TEST_PROFILE)
@DisplayName("BidragBeløpshistorikk")
@AutoConfigureWireMock(port = 0)
@EnableMockOAuth2Server
class BidragBeløpshistorikkApplicationTest {
    @Test
    fun `skal laste spring-context`() {
    }
}
