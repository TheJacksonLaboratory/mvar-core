package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Source)
abstract class SourceService {

    abstract Source get(Serializable id)

    abstract List<Source> list(Map args)

    abstract Long count()

    abstract void delete(Serializable id)

    abstract Source save(Source strain)

    Map<String, Object> query(Map params) {
        Map<String, Object> queryResults = [sourceList:[], sourceCount:0L]

        //max
        Integer max = params.max? Integer.valueOf(params.max): 10 as Integer
        //offset
        Long offset = params.offset? Long.valueOf(params.offset) : 0

        //sort by
        String orderBy = params.sortBy
        //sort direction
        String orderDirection = params.sortDirection? params.sortDirection: 'asc'

        println('query params: ' + params)

        //name
        def name = params.name

        // source version
        def sourceVersion = params.sourceVersion

        //generate query
        def results = Source.createCriteria().list ([max:max, offset:offset]) {

            if (name) {
                and {
                    eq('name', name)
                }
            }

            if (sourceVersion) {
                and {
                    eq('source_version', sourceVersion)
                }
            }
        }

        Long count = results.totalCount

        println("source search results count = " + count)
        log.info("source search results count = " + count)

        queryResults.sourceList = results
        queryResults.sourceCount = count

        return queryResults
    }
}
