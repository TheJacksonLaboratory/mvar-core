package org.jax.mvarcore

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.hibernate.SessionFactory
import org.hibernate.internal.SessionImpl

import java.sql.SQLException

@Transactional
class MvarStatService {

    SessionFactory sessionFactory

    def show() {

        MvarStat stat = new MvarStat()

        try {
            final Sql sql = getSql()
            def result = sql.rows("SELECT * from mvar_stat;")
            if (result) {
                stat.alleleCount = result.allele_count[0]
                stat.geneCount = result.gene_count[0]
                stat.strainCount = result.strain_count[0]
                stat.transcriptCount = result.transcript_count[0]
                stat.variantCount = result.variant_count[0]
                stat.variantStrainCount = result.variant_strain_count[0]
                stat.variantTranscriptCount = result.variant_transcript_count[0]
                stat.variantCanonIdentifierCount = result.variant_canon_identifier_count[0]
                stat.strainAnalysisCount = result.strain_analysis_count[0]
                stat.transcriptAnalysisCount = result.transcript_analysis_count[0]
                stat.geneAnalysisCount = result.gene_analysis_count[0]
                stat.assemblies = result.assemblies[0]
            }
        } catch (SQLException exc) {
            log.debug('The following SQLException occurred: ' + exc.toString())
        } finally {
            cleanUpGorm()
        }
        def list = []
        list << stat
        return list
    }

    protected Sql getSql() {
        SessionImpl sessionImpl = sessionFactory.currentSession as SessionImpl
        new Sql(sessionImpl.connection())
    }

    /**
     * Returns all sources in MVAR data
     * @return
     */
    List getSources() {
        def sources = []
        try {
            final Sql sql = getSql()
            sql.eachRow("select * from  source where id in (select distinct source_id from variant_source);") { row ->
                Source source = new Source()
                source.name = row.name
                source.sourceVersion = row.source_version
                source.url = row.url
                sources.add(source)
            }
        } catch (SQLException exc) {
            log.debug('The following SQLException occurred: ' + exc.toString())
        } finally {
            cleanUpGorm()
        }
        return sources
    }

    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
    }
}
