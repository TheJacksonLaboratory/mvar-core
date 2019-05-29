package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Allele)
interface AlleleService {

    Allele get(Serializable id)

    List<Allele> list(Map args)

    Long count()

    void delete(Serializable id)

    Allele save(Allele allele)

}