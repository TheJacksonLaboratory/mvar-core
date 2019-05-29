package org.jax.mvarcore

import grails.gorm.services.Service

@Service(Strain)
interface StrainService {

    Strain get(Serializable id)

    List<Strain> list(Map args)

    Long count()

    void delete(Serializable id)

    Strain save(Strain strain)

}