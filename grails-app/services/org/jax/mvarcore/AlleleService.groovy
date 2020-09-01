package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Allele)
abstract class AlleleService {

    abstract Allele get(Serializable id)

    abstract List<Allele> list(Map args)

    abstract Long count()

    abstract void delete(Serializable id)

    abstract Allele save(Allele gene)

    Map<String, Object> query(Map params) {

        Map<String, Object> queryResults = [alleleList:[], alleleCount:0L]

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

        //type
        def typeList = params.list('type')

        // primary identifier
        def primaryIdList = params.list('primary_id')

        //generate query
        def results = Allele.createCriteria().list ([max:max, offset:offset]) {

            if (nameList) {
                and {
                    inList('name', nameList)
                }
            }

            if (typeList) {
                and {
                    inList('type', typeList)
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

        println("allele search results count = " + count)
        log.info("allele search results count = " + count)

        queryResults.alleleList = results
        queryResults.alleleCount = count

        return queryResults
    }
}
