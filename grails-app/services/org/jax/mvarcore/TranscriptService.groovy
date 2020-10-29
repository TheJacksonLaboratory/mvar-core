package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Transcript)
abstract class TranscriptService {

    abstract Transcript get(Serializable id)

    abstract List<Transcript> list(Map args)

    abstract Long count()

    abstract void delete(Serializable id)

    abstract Transcript save(Transcript transcript)

    Map<String, Object> query(Map params) {

        Map<String, Object> queryResults = [transcriptList:[], transcriptCount:0L]

        //max
        Integer max = params.max? Integer.valueOf(params.max): 10 as Integer
        //offset
        Long offset = params.offset? Long.valueOf(params.offset) : 0

        //sort by
        String orderBy = params.sortBy
        //sort direction
        String orderDirection = params.sortDirection? params.sortDirection: 'asc'

        println('query params: ' + params)

        //chr
        def chrList = params.list('chr')

        //mgi gene id
        def mgiGeneIdList = params.list('mgi_gene_id')

        // primary identifier
        def primaryIdList = params.list('primary_id')

        //generate query
        def results = Transcript.createCriteria().list ([max:max, offset:offset]) {

            if (chrList) {
                and {
                    inList('chromosome', chrList)
                }
            }

            if (mgiGeneIdList) {
                and {
                    inList('mgi_gene_identifier', typeList)
                }
            }

            if (primaryIdList) {
                and {
                    inList('primary_identifier', primaryIdList)
                }
            }

            //handle order by
//            if (orderBy) {
//                if (orderBy == 'symbol') {
//                    gene {
//                        order('symbol', orderDirection)
//                    }
//                } else if (orderBy == 'strainId') {
//                    strain {
//                        order('sampleId', orderDirection)
//                    }
//                } else{
//                    order(orderBy, orderDirection)
//                }
//
//            }
        }

        Long count = results.totalCount

        println("transcript search results count = " + count)
        log.info("transcript search results count = " + count)

        queryResults.transcriptList = results
        queryResults.transcriptCount = count

        return queryResults
    }
}