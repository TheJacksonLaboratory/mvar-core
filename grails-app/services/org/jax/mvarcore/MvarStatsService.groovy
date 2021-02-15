package org.jax.mvarcore

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import groovy.json.JsonBuilder
import groovy.sql.Sql
import org.hibernate.SessionFactory
import org.hibernate.internal.SessionImpl

import java.sql.Connection
import java.sql.SQLException

@Transactional
class MvarStatsService {

    SessionFactory sessionFactory

    def show() {

        MvarStats stat = new MvarStats()

        try {
            final Sql sql = getSql()
            def result = sql.rows("SELECT * from mvar_stats;")
            stat.alleleCount = result.allele_count[0]
            stat.geneCount = result.gene_count[0]
            stat.strainCount = result.strain_count[0]
            stat.transcriptCount = result.transcript_count[0]
            stat.variantCount = result.variant_count[0]
            stat.variantStrainCount = result.variant_strain_count[0]
            stat.variantTranscriptCount = result.variant_transcript_count[0]
            stat.variantCanonIdentifierCount = result.variant_canon_identifier_count[0]

        } catch (SQLException exc) {

        } finally {
            cleanUpGorm()
        }

        // search for distinct strains and transcripts
        try {
            final Sql sql = getSql()
            def resultStrain = sql.rows("SELECT count(distinct strain_id) as strain_num FROM mvar_core.variant_strain;")
            stat.strainAnalysisCount = resultStrain.strain_num[0]

        } catch (SQLException exc) {

        } finally {
            cleanUpGorm()
        }
        try {
            final Sql sql = getSql()
            def resultTranscript = sql.rows("SELECT count(distinct transcript_id) as transcript_num FROM mvar_core.variant_transcript;")
            stat.transcriptAnalysisCount = resultTranscript.transcript_num[0]

        } catch (SQLException exc) {

        } finally {
            cleanUpGorm()
        }

        def list = []
        list << stat
        return list
    }

    def analysis(){

        // MVAR analysis info : when analysis is sent as a query in the url of the MVArStats endpoint
        // we query the relationship tables variantTranscript and variantStrain and check how many
        // unique strains there are and how many unique transcripts
        def result
        MvarStats stat = new MvarStats()
        // search for distinct strains
        try {
            final Sql sql = getSql()
            result = sql.rows("SELECT count(distinct strain_id) as strain_num FROM mvar_core.variant_strain;")
            stat.strainAnalysisCount = result.strain_num[0]

        } catch (SQLException exc) {

        } finally {
            cleanUpGorm()
        }
        // search for distinct transcripts
        try {
            final Sql sql = getSql()
            result = sql.rows("SELECT count(distinct transcript_id) as transcript_num FROM mvar_core.variant_transcript;")
            stat.transcriptAnalysisCount = result.transcript_num[0]

        } catch (SQLException exc) {

        } finally {
            cleanUpGorm()
        }
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
