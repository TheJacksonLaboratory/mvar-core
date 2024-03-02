package org.jax.mvarcore

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.hibernate.SessionFactory
import org.hibernate.internal.SessionImpl

import java.sql.Connection
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
                stat.sourceCount = result.source_count[0]
                stat.assemblyCount = result.assembly_count[0]
            }
        } catch (SQLException exc) {
            log.debug('The following SQLException occurred: ' + exc.toString())
        } finally {
            cleanUpGorm()
        }
//        def resultStrain = sql.rows("SELECT count(distinct strain_id) as strain_num FROM mvar_core.variant_strain;")
//        def resultTranscript = sql.rows("SELECT count(distinct transcript_id) as transcript_num FROM mvar_core.variant_transcript;")
//        def resultTranscript = sql.rows("SELECT count(distinct gene_id) as gene_num FROM mvar_core.variant;")
        def list = []
        list << stat
        return list
    }

    protected Sql getSql() {
        new Sql(getConnection())
    }

    /**
     * @return a Connection with the underlying connection for the active session
     */
    protected Connection getConnection() {
        SessionImpl sessionImpl = sessionFactory.currentSession as SessionImpl
        sessionImpl.connection()
    }

    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
    }
}
