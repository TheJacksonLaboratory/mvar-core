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

        // geneName
        def geneSymbolList = params.list('gene_symbol')
        //mRNA id - refseq ids and genebank
        def mRNAList = params.list('m_rna_id')

        // primary identifier
        def primaryIdList = params.list('primary_id')

        //generate query
        def results = Transcript.createCriteria().list ([max:max, offset:offset]) {

            if (geneSymbolList) {
                and {
                    inList('gene_symbol', geneSymbolList)
                }
            }
            if (mRNAList) {
                and {
                    inList('m_rna_id', mRNAList)
                }
            }

            if (primaryIdList) {
                and {
                    inList('primary_identifier', primaryIdList)
                }
            }

//            //handle order by
//            if (orderBy) {
//                if (orderBy == 'gene_symbol') {
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