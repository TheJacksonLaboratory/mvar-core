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

        //At least one of required parameters must be provided
        if (! params.genes && (! chr || ! startPos || ! endPos) && (! params.hgvsList) && (! params.mvarIdList) && (! params.dbSNPidList)) {
            return  queryResults
        }

        //CAID
        List<VariantCanonIdentifier> canonVarList = []
        for (id in params.mvarIdList){
            def vca = VariantCanonIdentifier.findByCaID(id)
            if (vca){
                canonVarList.push(vca)
            }
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
            if (params.dbSNPidList) {
                and {
                    inList ('accession', params.dbSNPidList)
                }
            }

        }

        Long count = results.totalCount

        // get all mvar strains
        List<MvarStrains> strainList = MvarStrains.all
        // infer all missing variantStrain distribution
        for (Variant variant : results) {
            Set<VariantStrain> variantStrainList = variant.variantStrains
            // put existing strains in variantStrain in a List
            List<Strain> existingStrains = new ArrayList<>()
            for (VariantStrain variantStrain : variantStrainList) {
                existingStrains.add(variantStrain.strain)
            }
            for(MvarStrains strain : strainList) {
                if (!existingStrains.contains(strain.strain)) {
                    VariantStrain variantStrain = new VariantStrain()
                    variantStrain.strain = strain.strain
                    variantStrain.variant = variant
                    variantStrain.genotype = '0/0'
                    variantStrainList.add(variantStrain)
                }
            }
        }
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

        def mvarStrains = MvarStrains.all

        strains = Strain.createCriteria().list(){
            and{
                inList('id', mvarStrains.collect { it.strainId})
            }
            order('name', 'asc')
        }
        return strains
    }

}

