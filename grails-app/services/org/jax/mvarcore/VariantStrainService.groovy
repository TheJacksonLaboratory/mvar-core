package org.jax.mvarcore

import grails.gorm.services.Service


@Service(VariantStrain)
class VariantStrainService {

    /**
     * Variant Strain query request
     * @param params
     * @return
     */
    Map<String, Object> query(Map params) {


        log.info('query params: ' + params)
        println(params)

        Map<String, Object> queryResults = [variantList: [], variantCount: 0L]

        //max
        Integer max = params.max ? Integer.valueOf(params.max) : 10 as Integer
        //offset
        Long offset = params.offset ? Long.valueOf(params.offset) : 0

        //REGION
        String chr = params.chr
        String startPos = params.startPos
        String endPos = params.endPos

        //Gene or postion coordinates must be provided
        if (! params.genes && (! chr || ! startPos || ! endPos) && (! params.hgvsList)) {
            return  queryResults
        }

        //CAID
        List<VariantCanonIdentifier> canonVarList = []
        if (params.mvarIdList) {
            canonVarList = VariantCanonIdentifier.findAllByCaIDInList(params.mvarIdList)
        }

        //generate query
        def results = Variant.createCriteria().list ([max:max, offset:offset]) {

            if (params.genes) {
                and {
                    gene {
                        inList('symbol', params.genes)
                    }
                }
            }

            if (chr) {
                and {
                    eq('chr', chr)
                }
            }

            if (startPos && endPos && startPos.isNumber() && endPos.isNumber()) {
                and {
                    between('position', startPos.toLong(), endPos.toLong())
                }
            }

            if (params.types){
                and{
                    inList('type', params.types)
                }
            }

            if (params.impacts) {
                for(String impact : params.impacts) {
                    and {
                        ilike('impact', '%' + impact + '%')
                    }
                }
            }

            if (params.consequences){
                for(String consequence : params.consequences) {
                    and {
                        ilike('functionalClassCode', '%' + consequence + '%')
                    }
                }
            }
            if (canonVarList) {
                and {
                    canonVarIdentifier {
                        inList("id", canonVarList.collect { it.id })
                    }
                }
            }
            if (params.hgvsList) {
                and {
                    inList('variantHgvsNotation', params.hgvsList)
                }
            }

        }

        Long count = results.totalCount
        queryResults.variantList = results
        queryResults.variantCount = count

        return queryResults

    }

    /**
     * find all unique strains with variants in the DB
     * @return
     */
    List<Strain> getDBStrains() {

        List<Strain> strains = []

        def criteria = VariantStrain.createCriteria()
        def distinctStrainIds =  criteria.list {
            projections {
                distinct('strain')
            }
        }

        if (distinctStrainIds){
            strains = Strain.createCriteria().list(){

                and{
                    inList('id', distinctStrainIds.collect { it.id })
                }

                order('name', 'asc')
            }
        }

        return strains
    }

}


