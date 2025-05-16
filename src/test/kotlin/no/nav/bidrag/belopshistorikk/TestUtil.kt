package no.nav.bidrag.belopshistorikk

import no.nav.bidrag.domene.enums.vedtak.Engangsbeløptype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.belopshistorikk.request.OpprettEngangsbeløpRequestDto
import no.nav.bidrag.transport.behandling.belopshistorikk.request.OpprettStønadRequestDto
import no.nav.bidrag.transport.behandling.belopshistorikk.request.OpprettStønadsperiodeRequestDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class TestUtil {
    companion object {
        fun byggStønadRequest() = OpprettStønadRequestDto(
            type = Stønadstype.BIDRAG,
            sak = Saksnummer("SAK-001"),
            skyldner = Personident("01018011111"),
            kravhaver = Personident("01010511111"),
            mottaker = Personident("01018211111"),
            nesteIndeksreguleringsår = 2024,
            innkreving = Innkrevingstype.MED_INNKREVING,
            opprettetAv = "X123456",
            periodeListe =
            listOf(
                OpprettStønadsperiodeRequestDto(
                    periode = ÅrMånedsperiode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-07-01")),
                    vedtaksid = 321,
                    gyldigFra = LocalDateTime.parse("2022-01-11T10:00:00.000001"),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = null,
                    beløp = BigDecimal.valueOf(3490),
                    valutakode = "NOK",
                    resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                ),
                OpprettStønadsperiodeRequestDto(
                    periode = ÅrMånedsperiode(LocalDate.parse("2019-07-01"), LocalDate.parse("2020-01-01")),
                    vedtaksid = 323,
                    gyldigFra = LocalDateTime.parse("2022-01-11T10:00:00.000001"),
                    gyldigTil = null,
                    periodeGjortUgyldigAvVedtaksid = null,
                    beløp = BigDecimal.valueOf(3520),
                    valutakode = "NOK",
                    resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                ),
            ),
        )

        fun byggEngangsbeløpRequest() = OpprettEngangsbeløpRequestDto(
            type = Engangsbeløptype.SÆRBIDRAG,
            sak = Saksnummer("SAK-001"),
            skyldner = Personident("Skyldner1"),
            kravhaver = Personident("Kravhaver1"),
            mottaker = Personident("Mottaker1"),
            vedtaksid = 1,
            gyldigFra = LocalDateTime.now(),
            gyldigTil = null,
            gjortUgyldigAvVedtaksid = null,
            beløp = BigDecimal.valueOf(5000),
            betaltBeløp = BigDecimal.ZERO,
            valutakode = "NOK",
            resultatkode = "SÆRBIDRAG_INNVILGET",
            innkreving = Innkrevingstype.MED_INNKREVING,
            referanse = "Referanse",
            opprettetAv = "TEST",
        )

        fun byggEngangsbeløpRequest2() = OpprettEngangsbeløpRequestDto(
            type = Engangsbeløptype.SÆRBIDRAG,
            sak = Saksnummer("SAK-001"),
            skyldner = Personident("Skyldner1"),
            kravhaver = Personident("Kravhaver1"),
            mottaker = Personident("Mottaker1"),
            vedtaksid = 2,
            gyldigFra = LocalDateTime.now(),
            gyldigTil = null,
            gjortUgyldigAvVedtaksid = null,
            beløp = BigDecimal.valueOf(6000),
            betaltBeløp = BigDecimal.ZERO,
            valutakode = "NOK",
            resultatkode = "SÆRBIDRAG_INNVILGET",
            innkreving = Innkrevingstype.MED_INNKREVING,
            referanse = "Referanse",
            opprettetAv = "TEST",
        )
    }
}
