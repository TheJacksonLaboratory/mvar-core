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

        Map<String, Object> queryResults = [variantList: [], variantCount: 0L]

        //max
        Integer max = params.max ? Integer.valueOf(params.max) : 10 as Integer
        //offset
        Long offset = params.offset ? Long.valueOf(params.offset) : 0

        //REGION
        String chr = params.chr
        String startPos = params.startPos
        String endPos = params.endPos

        //IMPACT
        def impactParams = params.impact
        //FUNCTIONAL CLASS / sequence ontology / annotation
        def functionalClassList = params.consequence


        //Gene or postion coordinates must be provided
        if (! params.genes && (! chr || ! startPos || ! endPos)) {
            return  queryResults
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

            if (impactParams) {
                and {
                    ilike('impact', '%' + impactParams + '%')
                }
            }

            if (functionalClassList){
                for(String functionalClass : functionalClassList) {
                    and {
                        ilike('functionalClassCode', '%' + functionalClass + '%')
                    }
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


