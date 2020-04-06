package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Synonym)
interface SynonymService {

    Synonym get(Serializable id)

    List<Synonym> list(Map args)

    Long count()

    void delete(Serializable id)

    Synonym save(Synonym strain)

}
