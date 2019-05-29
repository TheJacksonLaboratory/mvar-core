package org.jax.mvarcore

import grails.gorm.services.Service

@Service(VariantCanonIdentifier)
interface VariantCanonIdentifierService {

    VariantCanonIdentifier get(Serializable id)

    List<VariantCanonIdentifier> list(Map args)

    Long count()

    void delete(Serializable id)

    VariantCanonIdentifier save(VariantCanonIdentifier variantCanonIdentifier)

}