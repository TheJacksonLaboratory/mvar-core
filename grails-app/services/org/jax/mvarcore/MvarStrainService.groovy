package org.jax.mvarcore

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.hibernate.SessionFactory
import org.hibernate.internal.SessionImpl

import java.sql.Connection
import java.sql.SQLException

@Transactional
class MvarStrainService {

    SessionFactory sessionFactory

    def show() {
        def result
        List<MvarStrain> mvarStrains = []
        try {
            final Sql sql = getSql()
            result = sql.rows("SELECT * from mvar_strain;")
        } catch (SQLException exc) {
            log.debug('The following SQLException occurred: ' + exc.toString())
        } finally {
            cleanUpGorm()
        }
        for (def row : result) {
            MvarStrain strain = new MvarStrain()
            strain.name = row.name
            strain.strain = Strain.findById(row.strain_id)
            mvarStrains.add(strain)
        }
        return mvarStrains
    }

    Map<String, Object> query(Map params) {
        Map<String, Object> queryResults = [mvarStrainList:[], mvarStrainCount:0L]

        //max
        Integer max = params.max? Integer.valueOf(params.max): 10 as Integer
        //offset
        Long offset = params.offset? Long.valueOf(params.offset) : 0

        //sort by
        String orderBy = params.sortBy
        //sort direction
        String orderDirection = params.sortDirection? params.sortDirection: 'asc'

        println('query params: ' + params)

        //imputed
        def imputed = Byte.parseByte(params.imputed)

        //generate query
        def results = getMvarStrains(imputed)

        Long count = results.size

        println("mvarStrain search results count = " + count)
        log.info("mvarStrain search results count = " + count)

        queryResults.mvarStrainList = results
        queryResults.mvarStrainCount = count
        return queryResults
    }

    List<MvarStrain> getMvarStrains(def imputed) {
        // get all mvar strains
        def mvarStrains = []
        try {
            final Sql sql = getSql()
            sql.eachRow('SELECT id, name, strain_id from mvar_strain where id in (select mvar_strain_imputeds_id from mvar_strain_imputed where imputed_id in (select id from imputed where imputed=:imputed))', [imputed: imputed]) { row ->
                MvarStrain mvarStrain = new MvarStrain()
                mvarStrain.id = row.id
                mvarStrain.name = row.name
//                mvarStrain.strainId = row.strain_id
                mvarStrains.add(mvarStrain)
            }
        } catch (SQLException exc) {
            log.debug('The following SQLException occurred: ' + exc.toString())
        } finally {
            cleanUpGorm()
        }
        // sort by name
        return mvarStrains.sort{ ob1,ob2->ob1.name.toLowerCase() <=> ob2.name.toLowerCase() }
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
