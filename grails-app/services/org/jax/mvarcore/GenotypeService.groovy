package org.jax.mvarcore

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional

@Service(Genotype)
interface GenotypeService {

    Genotype get(Serializable id)

    List<Genotype> list(Map args)

    Long count()

    void delete(Serializable id)

    Genotype save(Genotype variant)
}
