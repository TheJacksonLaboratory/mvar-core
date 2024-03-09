package org.jax.mvarcore

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.hibernate.SessionFactory
import org.hibernate.internal.SessionImpl

import java.sql.SQLException

@Transactional
class SourceService {

    SessionFactory sessionFactory

    Map<String, Object> show() {
        Map<String, Object> queryResults = [sourceList:[], sourceCount:0L]
        //generate query
        def results = getSources("select * from source;")
        queryResults.sourceList = results
        queryResults.sourceCount = results.size()
        return queryResults
    }

    Map<String, Object> mvarSource() {
        Map<String, Object> queryResults = [sourceList:[], sourceCount:0L]
        def results = getSources("select * from  source where id in (select distinct source_id from variant_source);")
        queryResults.sourceList = results
        queryResults.sourceCount = results.size()
        return queryResults
    }

    /**
     * Returns sources given sql query on source table
     * @param query
     * @return
     */
    private List getSources(String query) {
        def sources = []
        try {
            SessionImpl sessionImpl = sessionFactory.currentSession as SessionImpl
            final Sql sql = new Sql(sessionImpl.connection())
            sql.eachRow(query) { row ->
                Source source = new Source()
                source.name = row.name
                source.sourceVersion = row.source_version
                source.url = row.url
                sources.add(source)
            }
        } catch (SQLException exc) {
            log.debug('The following SQLException occurred: ' + exc.toString())
        } finally {
            def session = sessionFactory.currentSession
            session.flush()
            session.clear()
        }
        return sources
    }

}
