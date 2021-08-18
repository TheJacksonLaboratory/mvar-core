package org.jax.mvarcore

class GenotypeTemp {

    String format
    String genotypeData
    int variantId
    static mapping = {
        version false
    }
    static constraints = {
        format nullable: false
        genotypeData nullable: false, sqlType: 'text'
    }
}
