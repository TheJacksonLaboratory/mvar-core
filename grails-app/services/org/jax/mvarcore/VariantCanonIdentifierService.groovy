package org.jax.mvarcore

import grails.gorm.services.Service

@Service(VariantCanonIdentifier)
abstract class VariantCanonIdentifierService {

    abstract VariantCanonIdentifier get(Serializable id)

    abstract List<VariantCanonIdentifier> list(Map args)

    abstract Long count()

    abstract void delete(Serializable id)

    abstract VariantCanonIdentifier save(VariantCanonIdentifier variantCanonIdentifier)

    Map<String, Object> query(Map params){

        Map<String, Object> queryResults = [variantCanonIdentifierList:[], variantCanonIdentifierCount:0L]

        //max
        Integer max = params.max? Integer.valueOf(params.max): 10 as Integer
        //offset
        Long offset = params.offset? Long.valueOf(params.offset) : 0

        //sort by
        String orderBy = params.sortBy
        //sort direction
        String orderDirection = params.sortDirection? params.sortDirection: 'asc'

        println('query params: ' + params)

        // canonical id
        def variantRefTxtList = params.list('variantRefTxt')

        //CAID
        def caidList = params.list('caid')
//        List<VariantCanonIdentifier> canonVarList = []
//        if (caid) {
//            canonVarList = VariantCanonIdentifier.findAllByCaIDInList(caid)
//        }

        //generate query
        def results = VariantCanonIdentifier.createCriteria().list ([max:max, offset:offset]) {

            if (variantRefTxtList){
                and {
                    inList('variantRefTxt', variantRefTxtList)
                }
            }

            if (caidList) {
                and {
                    inList("caID", caidList)
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

        println("variantCanonIdentifier search results count = " + count)
        log.info("variantCanonIdentifier search results count = " + count)

        queryResults.variantCanonIdentifierList = results
        queryResults.variantCanonIdentifierCount = count

        return queryResults

    }
}