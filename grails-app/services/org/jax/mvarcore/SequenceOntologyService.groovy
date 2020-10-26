package org.jax.mvarcore

import grails.gorm.services.Service

@Service(SequenceOntology)
abstract class SequenceOntologyService {

    abstract Long count()

    abstract SequenceOntology get(Serializable id)

    abstract List<SequenceOntology> list(Map args)

    Map<String, Object> query(Map params) {
        Map<String, Object> queryResults = [sequenceOntologyList:[], sequenceOntologyCount:0L]

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
        def nameList = params.list('name')

        //definition
        def synonymList = params.list('definition')

        //generate query
        def results = SequenceOntology.createCriteria().list ([max:max, offset:offset]) {

            if (nameList) {
                and {
                    inList('name', nameList)
                }
            }

            if (synonymList) {
                and {
                    inList('definition', synonymList)
                }
            }
        }

        Long count = results.totalCount

        println("Sequence Ontology search results count = " + count)
        log.info("Sequence Ontology search results count = " + count)

        queryResults.sequenceOntologyList = results
        queryResults.sequenceOntologyCount = count

        return queryResults
    }
}
