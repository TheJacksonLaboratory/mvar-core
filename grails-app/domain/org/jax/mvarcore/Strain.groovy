package org.jax.mvarcore

class Strain {

    String primaryIdentifier
    String name
    String attributes

    static hasMany = [variantStrains: VariantStrain, alleles: Allele, genotypes: Genotype]

    static constraints = {
        primaryIdentifier nullable: false
        primaryIdentifier unique: true
        name nullable: false
        attributes nullable: true
    }

    static mapping = {
        name index:'name_idx'
        primaryIdentifier index:'primary_identifier_idx'
        version false
    }
}
