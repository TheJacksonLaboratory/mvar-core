package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Variant)
interface VariantService {

    Variant get(Serializable id)

    List<Variant> list(Map args)

    Long count()

    void delete(Serializable id)

    Variant save(Variant variant)

}