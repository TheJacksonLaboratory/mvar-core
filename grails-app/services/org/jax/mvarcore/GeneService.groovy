package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Gene)
abstract class GeneService {

    abstract Gene get(Serializable id)

    abstract List<Gene> list(Map args)

    abstract Long count()

    abstract void delete(Serializable id)

    abstract Gene save(Gene gene)

    Map<String, Object> query(Map params) {

        Map<String, Object> queryResults = [geneList:[], geneCount:0L]

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

        // chr
        def chrList = params.list('chr')

        //type
        def typeList = params.list('type')

        // ensembl id
        def ensemblList = params.list('ensembl_id')

        //TYPE
        def mgiList = params.list('mgi_id')


        //generate query
        def results = Gene.createCriteria().list ([max:max, offset:offset]) {

            if (nameList) {
                and {
                    like('name', nameList)
                }
            }

            if (chrList) {
                and {
                    inList ('chr', chrList)
                }
            }

            if (typeList) {
                and {
                    inList('type', typeList)
                }
            }

            if (ensemblList) {
                and {
                    inList('ensembl_gene_id', ensemblList)
                }
            }

            if (mgiList) {
                and {
                    inList('mgi_id', mgiList)
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

        println("gene search results count = " + count)
        log.info("gene search results count = " + count)

        queryResults.geneList = results
        queryResults.geneCount = count

        return queryResults
    }

}
