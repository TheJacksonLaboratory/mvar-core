package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Transcript)
interface TranscriptService {

    Transcript get(Serializable id)

    List<Transcript> list(Map args)

    Long count()

    void delete(Serializable id)

    Transcript save(Transcript transcript)

}