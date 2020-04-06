package org.jax.mvarcore

class GenotypeTemp {

    String format
    String genotypeData
    static mapping = {
        version false
    }
    static constraints = {
        format nullable: false
        genotypeData nullable: false, sqlType: 'text'
    }
}
