package org.jax.mvarcore

import grails.gorm.services.Service


@Service(MvarGene)
abstract class MvarGeneService {

    abstract MvarGene get(Serializable id)

    abstract List<MvarGene> list(Map args)

    def getAllMvarGenes() {
        Map<String, Object> queryResults = [mvarGeneList:[], mvarGeneCount:0L]
        def mvarGenes = MvarGene.all
        queryResults.mvarGeneList = mvarGenes
        queryResults.mvarGeneCount = mvarGenes.size()
        return queryResults
    }
}
