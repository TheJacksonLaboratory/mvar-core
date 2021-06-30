package org.jax.mvarcore

class VariantStrain {

    String genotype

    static belongsTo = [strain: Strain, variant: Variant]
    static constraints = {
        genotype nullable: false
    }

    static mapping = {
        genotype sqlType: "char(5)"
        version false
    }
}
