package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Strain)
abstract class StrainService {

    abstract Strain get(Serializable id)

    abstract List<Strain> list(Map args)

    abstract Long count()

    abstract void delete(Serializable id)

    abstract Strain save(Strain strain)

    Map<String, Object> query(Map params) {

        Map<String, Object> queryResults = [strainList:[], strainCount:0L]

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

        //attributes
        def attributeList = params.list('attributes')

        // primary identifier
        def primaryIdList = params.list('primary_id')

        //generate query
        def results = Strain.createCriteria().list ([max:max, offset:offset]) {

            if (nameList) {
                and {
                    inList('name', nameList)
                }
            }

            if (attributeList) {
                and {
                    inList('attributes', attributeList)
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

        println("strain search results count = " + count)
        log.info("strain search results count = " + count)

        queryResults.strainList = results
        queryResults.strainCount = count

        return queryResults
    }
}