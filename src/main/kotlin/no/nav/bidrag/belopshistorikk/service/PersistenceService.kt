package no.nav.bidrag.belopshistorikk.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.annotation.Timed
import no.nav.bidrag.belopshistorikk.bo.PeriodeBo
import no.nav.bidrag.belopshistorikk.bo.toJustertPeriodeEntity
import no.nav.bidrag.belopshistorikk.bo.toPeriodeEntity
import no.nav.bidrag.belopshistorikk.persistence.entity.Engangsbeløp
import no.nav.bidrag.belopshistorikk.persistence.entity.Periode
import no.nav.bidrag.belopshistorikk.persistence.entity.Stønad
import no.nav.bidrag.belopshistorikk.persistence.entity.toEngangsbeløpEntity
import no.nav.bidrag.belopshistorikk.persistence.entity.toStønadEntity
import no.nav.bidrag.belopshistorikk.persistence.entity.toStønadPeriodeDto
import no.nav.bidrag.belopshistorikk.persistence.repository.EngangsbeløpRepository
import no.nav.bidrag.belopshistorikk.persistence.repository.PeriodeRepository
import no.nav.bidrag.belopshistorikk.persistence.repository.StønadRepository
import no.nav.bidrag.commons.util.secureLogger
import no.nav.bidrag.transport.behandling.belopshistorikk.request.OpprettEngangsbeløpRequestDto
import no.nav.bidrag.transport.behandling.belopshistorikk.request.OpprettStønadRequestDto
import no.nav.bidrag.transport.behandling.belopshistorikk.response.StønadPeriodeDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime
private val LOGGER = KotlinLogging.logger {}

@Service
class PersistenceService(
    val stønadRepository: StønadRepository,
    val periodeRepository: PeriodeRepository,
    val engangsbeløpRepository: EngangsbeløpRepository,
) {

    @Timed
    fun opprettStønad(opprettStønadRequestDto: OpprettStønadRequestDto): Int {
        val nyStønad = opprettStønadRequestDto.toStønadEntity()
        val stønad = stønadRepository.save(nyStønad)
        return stønad.stønadsid!!
    }

    @Timed
    fun oppdaterStønad(stønadsid: Int, opprettetAv: String, nesteIndeksreguleringsår: Int?) {
        stønadRepository.oppdaterStønad(stønadsid = stønadsid, opprettetAv = opprettetAv, nesteIndeksreguleringsår = nesteIndeksreguleringsår)
    }

    fun opprettPeriode(periodeBo: PeriodeBo, stønadsid: Int) {
        val eksisterendeStønad =
            stønadRepository.findById(stønadsid)
                .orElseThrow {
                    IllegalArgumentException(
                        String.format(
                            "Fant ikke stønad med id %d i databasen",
                            stønadsid,
                        ),
                    )
                }
        val nyPeriode = periodeBo.toPeriodeEntity(eksisterendeStønad)
        periodeRepository.save(nyPeriode)
    }

    fun opprettJustertPeriode(periodeBo: PeriodeBo, stønadsid: Int, vedtakstidspunkt: LocalDateTime) {
        val eksisterendeStønad =
            stønadRepository.findById(stønadsid)
                .orElseThrow {
                    IllegalArgumentException(
                        String.format(
                            "Fant ikke stønad med id %d i databasen",
                            stønadsid,
                        ),
                    )
                }
        val nyPeriode = periodeBo.toJustertPeriodeEntity(eksisterendeStønad = eksisterendeStønad, vedtakstidspunkt = vedtakstidspunkt)
        periodeRepository.save(nyPeriode)
    }

    @Timed
    fun hentStønadFraId(stønadsid: Int): Stønad? {
        val stønad =
            stønadRepository.findById(stønadsid)
                .orElseThrow {
                    IllegalArgumentException(
                        String.format(
                            "Fant ikke stønad med id %d i databasen",
                            stønadsid,
                        ),
                    )
                }
        return stønad
    }

    @Timed
    fun hentStønad(stønadType: String, skyldner: String, kravhaver: String, sak: String): Stønad? =
        stønadRepository.finnStønad(stønadstype = stønadType, skyldner = skyldner, kravhaver = kravhaver, sak = sak)

    fun hentStønaderForSak(sak: String): List<Stønad> = stønadRepository.finnStønaderForSak(sak)

    @Timed
    fun finnBidragssakerForSkyldner(skyldner: String): List<Stønad> = stønadRepository.finnBidragssakerForSkyldner(skyldner)
    fun finnAlleStønaderForSkyldner(skyldner: String): List<Stønad> = stønadRepository.finnAlleStønaderForSkyldner(skyldner)

    fun hentPerioderForStønad(id: Int): List<Periode> = periodeRepository.hentGyldigePerioderForStønad(id)

    fun hentPerioderForStønadInkludertUgyldiggjorte(id: Int): List<Periode> = periodeRepository.hentPerioderForStønadInkludertUgyldiggjorte(id)

    fun endreMottakerForStønad(stønadsid: Int, nyMottaker: String, opprettetAv: String) {
        LOGGER.info("Oppdaterer mottaker for stønadsid $stønadsid")
        secureLogger.debug { "Oppdaterer mottaker for stønadsid $stønadsid" }
        stønadRepository.endreMottakerForStønad(stønadsid = stønadsid, mottaker = nyMottaker, opprettetAv = opprettetAv)
    }

    fun settPeriodeSomUgyldig(periodeId: Int, periodeGjortUgyldigAvVedtaksid: Int, vedtakstidspunkt: LocalDateTime) {
        periodeRepository.settPeriodeSomUgyldig(
            periodeid = periodeId,
            periodeGjortUgyldigAvVedtaksid = periodeGjortUgyldigAvVedtaksid,
            vedtakstidspunkt = vedtakstidspunkt,
        )
    }

    fun hentPeriode(id: Int): StønadPeriodeDto? {
        val periode =
            periodeRepository.findById(id)
                .orElseThrow {
                    IllegalArgumentException(
                        String.format(
                            "Fant ikke periode med id %d i databasen",
                            id,
                        ),
                    )
                }
        return periode.toStønadPeriodeDto()
    }

    fun hentPerioderForStønadForAngittTidspunkt(id: Int, gyldigTidspunkt: LocalDateTime): List<Periode> =
        periodeRepository.hentGyldigePerioderForStønadForAngittTidspunkt(stønadsid = id, gyldigTidspunkt = gyldigTidspunkt)

    @Timed
    fun opprettEngangsbeløp(opprettEngangsbeløpRequestDto: OpprettEngangsbeløpRequestDto): Int {
        val nyttEngangsbeløp = opprettEngangsbeløpRequestDto.toEngangsbeløpEntity()
        val engangsbeløp = engangsbeløpRepository.save(nyttEngangsbeløp)
        return engangsbeløp.engangsbeløpsid!!
    }

    @Timed
    fun hentEngangsbeløp(engangsbeløpType: String, skyldner: String, kravhaver: String, sak: String, referanse: String): Engangsbeløp? =
        engangsbeløpRepository.finnEngangsbeløp(
            engangsbeløpstype = engangsbeløpType,
            skyldner = skyldner,
            kravhaver = kravhaver,
            sak = sak,
            referanse = referanse,
        )

    @Timed
    fun hentHistoriskeEngangsbeløp(
        engangsbeløpType: String,
        skyldner: String,
        kravhaver: String,
        sak: String,
        referanse: String,
    ): List<Engangsbeløp> = engangsbeløpRepository.finnHistoriskeEngangsbeløp(
        engangsbeløpstype = engangsbeløpType,
        skyldner = skyldner,
        kravhaver = kravhaver,
        sak = sak,
        referanse = referanse,
    )

    fun endreMottakerForEngangsbeløp(engangsbeløpsid: Int, nyMottaker: String, opprettetAv: String) {
        LOGGER.info("Oppdaterer mottaker for engangsbeløpsid $engangsbeløpsid")
        secureLogger.debug { "Oppdaterer mottaker for engangsbeløpsid $engangsbeløpsid" }
        engangsbeløpRepository.endreMottakerForEngangsbeløp(engangsbeløpsid = engangsbeløpsid, mottaker = nyMottaker, opprettetAv = opprettetAv)
    }

    fun settEngangsbeløpSomUgyldig(engangsbeløpId: Int, gjortUgyldigAvVedtaksid: Int, vedtakstidspunkt: LocalDateTime, endretAv: String) {
        engangsbeløpRepository.settEngangsbeløpSomUgyldig(
            engangsbeløpsid = engangsbeløpId,
            gjortUgyldigAvVedtaksid = gjortUgyldigAvVedtaksid,
            vedtakstidspunkt = vedtakstidspunkt,
            endretAv = endretAv,
        )
    }
}
