package no.nav.bidrag.belopshistorikk.service

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.bidrag.belopshistorikk.bo.OppdatertPeriode
import no.nav.bidrag.belopshistorikk.bo.PeriodeBo
import no.nav.bidrag.belopshistorikk.bo.toPeriodeBo
import no.nav.bidrag.belopshistorikk.persistence.entity.toEngangsbeløpDto
import no.nav.bidrag.belopshistorikk.persistence.entity.toStønadDto
import no.nav.bidrag.belopshistorikk.persistence.entity.toStønadPeriodeDto
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.belopshistorikk.request.HentEngangsbeløpRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.request.HentStønadHistoriskRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.request.HentStønadRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.request.LøpendeBidragssakerRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.request.OpprettEngangsbeløpRequestDto
import no.nav.bidrag.transport.behandling.belopshistorikk.request.OpprettStønadRequestDto
import no.nav.bidrag.transport.behandling.belopshistorikk.request.OpprettStønadsperiodeRequestDto
import no.nav.bidrag.transport.behandling.belopshistorikk.request.SkyldnerStønaderRequest
import no.nav.bidrag.transport.behandling.belopshistorikk.response.EngangsbeløpDto
import no.nav.bidrag.transport.behandling.belopshistorikk.response.LøpendeBidragssak
import no.nav.bidrag.transport.behandling.belopshistorikk.response.LøpendeBidragssakerResponse
import no.nav.bidrag.transport.behandling.belopshistorikk.response.PeriodeBeløp
import no.nav.bidrag.transport.behandling.belopshistorikk.response.SkyldnerStønad
import no.nav.bidrag.transport.behandling.belopshistorikk.response.SkyldnerStønaderResponse
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadDto
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadMedPeriodeBeløpResponse
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadPeriodeDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.YearMonth
private val LOGGER = KotlinLogging.logger {}

@Service
@Transactional
class BeløpshistorikkService(val persistenceService: PersistenceService) {

    // Opprett komplett stønad (alle tabeller)
    fun opprettStønad(stønadRequest: OpprettStønadRequestDto): Int {
        val opprettetStønadId = persistenceService.opprettStønad(stønadRequest)
        // Perioder
        stønadRequest.periodeListe.forEach { opprettPeriode(periodeRequest = it, stønadsid = opprettetStønadId) }
        return opprettetStønadId
    }

    // Opprett periode
    private fun opprettPeriode(periodeRequest: OpprettStønadsperiodeRequestDto, stønadsid: Int) {
        persistenceService.opprettPeriode(periodeBo = periodeRequest.toPeriodeBo(), stønadsid = stønadsid)
    }

    // Henter stønad ut fra stønadsid
    fun hentStønadFraId(stønadsid: Int): StønadDto? {
        val stønad = persistenceService.hentStønadFraId(stønadsid)
        if (stønad != null) {
            val stønadPeriodeDtoListe = mutableListOf<StønadPeriodeDto>()
            val periodeListe = persistenceService.hentPerioderForStønad(stønadsid)
            periodeListe.forEach { periode ->
                stønadPeriodeDtoListe.add(periode.toStønadPeriodeDto())
            }
            return stønad.toStønadDto(stønadPeriodeDtoListe)
        } else {
            return null
        }
    }

    // Henter stønad ut fra unik nøkkel for stønad
    fun hentStønad(request: HentStønadRequest): StønadDto? {
        val stønad =
            persistenceService.hentStønad(
                stønadType = request.type.toString(),
                skyldner = request.skyldner.verdi,
                kravhaver = request.kravhaver.verdi,
                sak = request.sak.toString(),
            )
        if (stønad != null) {
            val stønadPeriodeDtoListe = mutableListOf<StønadPeriodeDto>()
            val periodeListe = persistenceService.hentPerioderForStønad(stønad.stønadsid!!)
            periodeListe.forEach { periode ->
                stønadPeriodeDtoListe.add(periode.toStønadPeriodeDto())
            }
            return stønad.toStønadDto(stønadPeriodeDtoListe)
        } else {
            return null
        }
    }

    fun hentStønadInkludertUgyldiggjortePerioder(stønadstype: String, skyldner: String, kravhaver: String, sak: String): StønadDto? {
        val stønad = persistenceService.hentStønad(stønadType = stønadstype, skyldner = skyldner, kravhaver = kravhaver, sak = sak)
        if (stønad != null) {
            val stønadPeriodeDtoListe = mutableListOf<StønadPeriodeDto>()
            val periodeListe =
                persistenceService.hentPerioderForStønadInkludertUgyldiggjorte(stønad.stønadsid!!)
            periodeListe.forEach { periode ->
                stønadPeriodeDtoListe.add(periode.toStønadPeriodeDto())
            }
            return stønad.toStønadDto(stønadPeriodeDtoListe)
        } else {
            return null
        }
    }

    fun hentStønadHistorisk(request: HentStønadHistoriskRequest): StønadDto? {
        val stønad =
            persistenceService.hentStønad(
                stønadType = request.type.toString(),
                skyldner = request.skyldner.verdi,
                kravhaver = request.kravhaver.verdi,
                sak = request.sak.toString(),
            )
        if (stønad != null) {
            val stønadPeriodeDtoListe = mutableListOf<StønadPeriodeDto>()
            val periodeListe =
                persistenceService.hentPerioderForStønadForAngittTidspunkt(id = stønad.stønadsid!!, gyldigTidspunkt = request.gyldigTidspunkt)
            periodeListe.forEach { periode ->
                stønadPeriodeDtoListe.add(periode.toStønadPeriodeDto())
            }
            return stønad.toStønadDto(stønadPeriodeDtoListe)
        } else {
            return null
        }
    }

    // Henter alle stønad for angitt sak
    fun hentStønaderForSak(sak: String): List<StønadDto> {
        val stønadListe = persistenceService.hentStønaderForSak(sak)
        if (stønadListe.isNotEmpty()) {
            val stønadsendringDtoListe = mutableListOf<StønadDto>()
            stønadListe.forEach { stønad ->
                val stønadPeriodeDtoListe = mutableListOf<StønadPeriodeDto>()
                val periodeListe = persistenceService.hentPerioderForStønad(stønad.stønadsid!!)
                periodeListe.forEach { periode ->
                    stønadPeriodeDtoListe.add(periode.toStønadPeriodeDto())
                }
                stønadsendringDtoListe.add(stønad.toStønadDto(stønadPeriodeDtoListe))
            }
            return stønadsendringDtoListe
        } else {
            return emptyList()
        }
    }

    fun endreStønad(eksisterendeStønad: StønadDto, oppdatertStønad: OpprettStønadRequestDto, vedtakstidspunkt: LocalDateTime) {
        val stønadsid = eksisterendeStønad.stønadsid
        val endretAvSaksbehandlerId = oppdatertStønad.opprettetAv
        val nesteIndeksreguleringsår = oppdatertStønad.nesteIndeksreguleringsår

        persistenceService.oppdaterStønad(
            stønadsid = stønadsid,
            opprettetAv = endretAvSaksbehandlerId,
            nesteIndeksreguleringsår = nesteIndeksreguleringsår,
        )

        val oppdatertStønadVedtakId = oppdatertStønad.periodeListe.first().vedtaksid

        eksisterendeStønad.periodeListe.forEach { periode ->
            val justertPeriode = finnOverlappPeriode(eksisterendePeriode = periode.toPeriodeBo(), oppdatertStønad = oppdatertStønad)
            if (justertPeriode.settPeriodeSomUgyldig) {
                // Setter opprinnelige periode som ugyldig
                persistenceService.settPeriodeSomUgyldig(
                    periodeId = periode.periodeid,
                    periodeGjortUgyldigAvVedtaksid = oppdatertStønadVedtakId,
                    vedtakstidspunkt = vedtakstidspunkt,
                )
            }
            // Sjekker om det skal opprettes en ny periode med justerte datoer tilpasset perioder i nytt vedtak
            if (justertPeriode.oppdaterPerioder) {
                justertPeriode.periodeListe.forEach {
                    persistenceService.opprettJustertPeriode(periodeBo = it, stønadsid = stønadsid, vedtakstidspunkt = vedtakstidspunkt)
                }
            }
        }

        oppdatertStønad.periodeListe.forEach {
            // Sjekk om beløp for ny periode = null, det er da et opphørsvedtak og periode skal ikke lagres.
            // Sjekken må gjøres etter at de eksisterende periodene er behandlet
            if (it.beløp != null) {
                persistenceService.opprettPeriode(periodeBo = it.toPeriodeBo(), stønadsid = stønadsid)
            }
        }
    }

    fun finnLøpendeBidragssaker(request: LøpendeBidragssakerRequest): LøpendeBidragssakerResponse {
        val stønader =
            persistenceService.finnBidragssakerForSkyldner(request.skyldner.verdi)

        val løpendeBidragssakListe = mutableListOf<LøpendeBidragssak>()

        stønader.forEach { stønad ->
            val periode =
                persistenceService.hentPerioderForStønad(stønad.stønadsid!!)
                    .filter { it.fom.isBefore(request.dato.plusDays(1)) && (it.til == null || it.til.isAfter(request.dato)) }
                    .maxByOrNull { it.fom }
            // periode er tom hvis det ikke finnes en periode for stønaden som er aktiv på angitt dato
            if (periode != null) {
                løpendeBidragssakListe.add(
                    LøpendeBidragssak(
                        sak = Saksnummer(stønad.sak),
                        type = Stønadstype.valueOf(stønad.type),
                        kravhaver = Personident(stønad.kravhaver),
                        løpendeBeløp = periode.beløp ?: BigDecimal.ZERO,
                        valutakode = periode.valutakode ?: "NOK",
                    ),
                )
            }
        }
        return LøpendeBidragssakerResponse(løpendeBidragssakListe)
    }

    fun finnAlleStønaderForSkyldner(request: SkyldnerStønaderRequest): SkyldnerStønaderResponse {
        val stønader =
            persistenceService.finnAlleStønaderForSkyldner(request.skyldner.verdi)

        val stønaderListe = stønader.map { stønad ->
            SkyldnerStønad(
                sak = Saksnummer(stønad.sak),
                type = Stønadstype.valueOf(stønad.type),
                kravhaver = Personident(stønad.kravhaver),
            )
        }
        return SkyldnerStønaderResponse(stønaderListe)
    }

    // Henter stønad ut fra unik nøkkel for stønad
    fun hentStønadMedPeriodebeløp(request: HentStønadRequest): StønadMedPeriodeBeløpResponse? {
        val stønad =
            persistenceService.hentStønad(
                stønadType = request.type.toString(),
                skyldner = request.skyldner.verdi,
                kravhaver = request.kravhaver.verdi,
                sak = request.sak.toString(),
            )

        if (stønad != null) {
            val periodeListe = persistenceService.hentPerioderForStønad(stønad.stønadsid!!)
            return StønadMedPeriodeBeløpResponse(
                førsteIndeksreguleringsår = stønad.nesteIndeksreguleringsår,
                nesteIndeksreguleringsår = stønad.nesteIndeksreguleringsår,
                periodeBeløpListe = periodeListe.map { periode ->
                    PeriodeBeløp(
                        periode = ÅrMånedsperiode(fom = periode.fom, til = periode.til),
                        beløp = periode.beløp,
                        valutakode = periode.valutakode,
                    )
                },
            )
        } else {
            return null
        }
    }

    private fun finnOverlappPeriode(eksisterendePeriode: PeriodeBo, oppdatertStønad: OpprettStønadRequestDto): OppdatertPeriode {
        val periodeBoListe = mutableListOf<PeriodeBo>()
        val oppdatertStønadDatoFom = oppdatertStønad.periodeListe.first().periode.fom
        val oppdatertStønadDatoTil = oppdatertStønad.periodeListe.last().periode.til
        if (eksisterendePeriode.periode.fom.isBefore(oppdatertStønadDatoFom)) {
            if (eksisterendePeriode.periode.til == null || eksisterendePeriode.periode.til!!.isAfter(oppdatertStønadDatoFom)) {
                // Perioden overlapper. Eksisterende periode må settes som ugyldig og ny periode opprettes med korrigert til-dato.
                periodeBoListe.add(lagNyPeriodeMedEndretTilDato(periode = eksisterendePeriode, nyTilDato = oppdatertStønadDatoFom))
                if (oppdatertStønadDatoTil != null &&
                    (
                        eksisterendePeriode.periode.til == null ||
                            eksisterendePeriode.periode.til!!
                                .isAfter(oppdatertStønadDatoTil)
                        )
                ) {
                    periodeBoListe.add(lagNyPeriodeMedEndretFomDato(periode = eksisterendePeriode, nyFomDato = oppdatertStønadDatoTil))
                }
                return OppdatertPeriode(periodeListe = periodeBoListe, oppdaterPerioder = true, settPeriodeSomUgyldig = true)
            }
        } else if (oppdatertStønadDatoTil == null) {
            periodeBoListe.add(eksisterendePeriode)
            return OppdatertPeriode(periodeListe = periodeBoListe, oppdaterPerioder = false, settPeriodeSomUgyldig = true)
        } else if (eksisterendePeriode.periode.fom.isAfter(oppdatertStønadDatoTil.minusMonths(1))) {
            periodeBoListe.add(eksisterendePeriode)
            return OppdatertPeriode(periodeListe = periodeBoListe, oppdaterPerioder = false, settPeriodeSomUgyldig = false)
        } else if (eksisterendePeriode.periode.til == null || eksisterendePeriode.periode.til!!.isAfter(oppdatertStønadDatoTil)) {
            periodeBoListe.add(lagNyPeriodeMedEndretFomDato(periode = eksisterendePeriode, nyFomDato = oppdatertStønadDatoTil))
            return OppdatertPeriode(periodeListe = periodeBoListe, oppdaterPerioder = true, settPeriodeSomUgyldig = true)
        } else if (eksisterendePeriode.periode.til!!.isBefore(oppdatertStønadDatoTil.plusMonths(1))) {
            periodeBoListe.add(eksisterendePeriode)
            return OppdatertPeriode(periodeListe = periodeBoListe, oppdaterPerioder = false, settPeriodeSomUgyldig = true)
        } else {
            periodeBoListe.add(eksisterendePeriode)
        }
        return OppdatertPeriode(periodeListe = periodeBoListe, oppdaterPerioder = false, settPeriodeSomUgyldig = false)
    }

    fun lagNyPeriodeMedEndretFomDato(periode: PeriodeBo, nyFomDato: YearMonth): PeriodeBo = PeriodeBo(
        periode = ÅrMånedsperiode(fom = nyFomDato, til = periode.periode.til),
        stønadsid = periode.stønadsid,
        vedtaksid = periode.vedtaksid,
        periodeGjortUgyldigAvVedtaksid = null,
        beløp = periode.beløp,
        valutakode = periode.valutakode,
        resultatkode = periode.resultatkode,
    )

    fun lagNyPeriodeMedEndretTilDato(periode: PeriodeBo, nyTilDato: YearMonth): PeriodeBo = PeriodeBo(
        periode = ÅrMånedsperiode(fom = periode.periode.fom, til = nyTilDato),
        stønadsid = periode.stønadsid,
        vedtaksid = periode.vedtaksid,
        periodeGjortUgyldigAvVedtaksid = null,
        beløp = periode.beløp,
        valutakode = periode.valutakode,
        resultatkode = periode.resultatkode,
    )

    // Oppretter engangsbeløp
    fun opprettEngangsbeløp(engangsbeløpRequest: OpprettEngangsbeløpRequestDto): Int {
        LOGGER.info("Oppretter nytt engangsbeløp for vedtak med id ${engangsbeløpRequest.vedtaksid}")
        secureLogger.debug { "Oppretter nytt engangsbeløp: ${tilJson(engangsbeløpRequest)}" }
        return persistenceService.opprettEngangsbeløp(engangsbeløpRequest)
    }

    // Henter engangsbeløp ut fra unik nøkkel
    fun hentEngangsbeløp(request: HentEngangsbeløpRequest): EngangsbeløpDto? {
        val engangsbeløp =
            persistenceService.hentEngangsbeløp(
                engangsbeløpType = request.type.toString(),
                skyldner = request.skyldner.verdi,
                kravhaver = request.kravhaver.verdi,
                sak = request.sak.toString(),
                referanse = request.referanse,
            )
        return engangsbeløp?.toEngangsbeløpDto()
    }

    // Henter historiske engangsbeløp
    fun hentHistoriskeEngangsbeløp(request: HentEngangsbeløpRequest): List<EngangsbeløpDto> {
        val engangsbeløpListe =
            persistenceService.hentHistoriskeEngangsbeløp(
                engangsbeløpType = request.type.toString(),
                skyldner = request.skyldner.verdi,
                kravhaver = request.kravhaver.verdi,
                sak = request.sak.toString(),
                referanse = request.referanse,
            )
        if (engangsbeløpListe.isNotEmpty()) {
            val engangsbeløpDtoListe = mutableListOf<EngangsbeløpDto>()
            engangsbeløpListe.forEach { engangsbeløp ->
                engangsbeløpDtoListe.add(engangsbeløp.toEngangsbeløpDto())
            }
            return engangsbeløpDtoListe
        } else {
            return emptyList()
        }
    }

    fun endreEngangsbeløp(
        eksisterendeEngangsbeløp: EngangsbeløpDto,
        oppdatertEngangsbeløp: OpprettEngangsbeløpRequestDto,
        vedtaksid: Int,
        vedtakstidspunkt: LocalDateTime,
    ) {
        val engangsbeløpsid = eksisterendeEngangsbeløp.engangsbeløpsid
        LOGGER.info("Setter engangsbeløp med id $engangsbeløpsid som ugyldig")
        secureLogger.debug { "Setter engangsbeløp som ugyldig: ${tilJson(eksisterendeEngangsbeløp)}" }
        persistenceService.settEngangsbeløpSomUgyldig(
            engangsbeløpId = engangsbeløpsid,
            gjortUgyldigAvVedtaksid = vedtaksid,
            vedtakstidspunkt = vedtakstidspunkt,
            endretAv = oppdatertEngangsbeløp.opprettetAv,
        )
        if (oppdatertEngangsbeløp.beløp != null) {
            LOGGER.info("Oppretter nytt engangsbeløp")
            secureLogger.debug { "Oppretter nytt engangsbeløp: ${tilJson(oppdatertEngangsbeløp)}" }
            persistenceService.opprettEngangsbeløp(oppdatertEngangsbeløp)
        } else {
            // Skal ikke opprette nytt engangsbeløp hvis det er null
            LOGGER.info("Nytt engangsbeløp er null, opprettes ikke på nytt")
            secureLogger.debug { "Nytt engangsbeløp er null, opprettes ikke på nytt" }
        }
    }

    fun hentPeriode(periodeId: Int): StønadPeriodeDto? = persistenceService.hentPeriode(periodeId)
}
