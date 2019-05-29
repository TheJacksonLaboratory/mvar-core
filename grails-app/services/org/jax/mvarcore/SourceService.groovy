package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Source)
interface SourceService {

    Source get(Serializable id)

    List<Source> list(Map args)

    Long count()

    void delete(Serializable id)

    Source save(Source source)

}