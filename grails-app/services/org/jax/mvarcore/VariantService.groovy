package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Variant)
abstract class VariantService {

    abstract Variant get(Serializable id)

    abstract List<Variant> list(Map args)

    abstract Long count()

    abstract void delete(Serializable id)

    abstract Variant save(Variant variant)

    Map<String, Object> query(Map params){

        Map<String, Object> queryResults = [variantList:[], variantCount:0L]

        //max
        Integer max = params.max? Integer.valueOf(params.max): 10 as Integer
        //offset
        Long offset = params.offset? Long.valueOf(params.offset) : 0

        //sort by
        String orderBy = params.sortBy
        //sort direction
        String orderDirection = params.sortDirection? params.sortDirection: 'asc'

        println('query params: ' + params)

        //GENES
        def geneParams = params.list('gene')

        List<Gene> geneList = []
        if (geneParams){
            geneList = Gene.findAllBySymbolInList(geneParams)
        }

        //REGION
        String chr = params.chr
        String startPos = params.startPos
        String endPos = params.endPos

        //STRAINS
        def strainParams = params.list('strain')

        List<Variant> strainVariantList = []
        if (strainParams){
            strainVariantList = Strain.findAllByNameInList(strainParams)
        }

        // canonical id
        def variantRefTxtList = params.list('variantRefTxt')

        // allele symbol
        def alleleParams = params.list('allele')
        List<Allele> alleleList = []
        if (alleleParams) {
            alleleList = Allele.findAllBySymbolInListAndId(alleleParams)
        }
        //TYPE
        def varTypeList = params.list('type')

        //IMPACT
        def impactParams = params.list('impact')
        //FUNCTIONAL CLASS
        def functionalClassList = params.list('functionalClassCode')

        //CAID
        def caid = params.caid
        List<VariantCanonIdentifier> canonVarList = []
        if (caid) {
            canonVarList = VariantCanonIdentifier.findAllByCaID(caid)
        }

        // TODO support chromosome range

        //generate query
        def results = Variant.createCriteria().list ([max:max, offset:offset]) {

            if (geneList) {
                and {
                    gene {
                        inList('id', geneList.collect { it.id })
                    }
                }
            }

            if (strainVariantList) {
                and {
                    strains {
                        inList('id', strainVariantList.collect { it.id })
                    }
                }
            }

            if (chr){
                and {
                    eq('chr', chr)
                }
            }

            if (startPos && endPos && startPos.isNumber() && endPos.isNumber()){
                and{
                    between('position', startPos.toLong(), endPos.toLong())
                }
            }

            if (impactParams){
                and {
                    inList ('impact', impactParams)
                }
            }

            if (variantRefTxtList){
                and {
                    inList ('variantRefTxt', variantRefTxtList)
                }
            }

            if (alleleList) {
                // TODO
            }

            if (canonVarList) {
                and {
                    canonVarIdentifier {
                        inList("id", canonVarList.collect { it.id })
                    }
                }
            }
            if (varTypeList){
                and{
                    inList('type', varTypeList)
                }
            }

            if (functionalClassList){
                and {
                    inList('functionalClassCode', functionalClassList)
                }
            }

            if (startPos && endPos && startPos.isNumber() && endPos.isNumber()){
                and{
                    between('position', startPos.toLong(), endPos.toLong())
                }
            }

            //handle order by
            if (orderBy) {
                if (orderBy == 'symbol') {
                    gene {
                        order('symbol', orderDirection)
                    }
                } else if (orderBy == 'pos' || orderBy =='hgvs') {
                    canonVarIdentifier {
                        order('position', orderDirection)
                    }
                } else if (orderBy == 'caid') {
                    canonVarIdentifier {
                        order('caID', orderDirection)
                    }
                } else {
                    order(orderBy, orderDirection)
                }

            }
        }

        Long count = results.totalCount

        println("variant search results count = " + count)
        log.info("variant search results count = " + count)

        queryResults.variantList = results
        queryResults.variantCount = count

        return queryResults

    }
}