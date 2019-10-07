package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Gene)
interface GeneService {

    Gene get(Serializable id)

    List<Gene> list(Map args)

    Long count()

    void delete(Serializable id)

    Gene save(Gene gene)

}
