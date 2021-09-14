package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Imputed)
abstract class ImputedService {
    abstract Imputed get(Serializable id)

    abstract List<Imputed> list(Map args)

    abstract Long count()
}
