package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Identifier)
interface IdentifierService {

    Identifier get(Serializable id)

    List<Identifier> list(Map args)

    Long count()

    void delete(Serializable id)

    Identifier save(Identifier identifier)

}
