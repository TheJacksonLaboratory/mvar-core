package org.jax.mvarcore

class Strain {

    String primaryIdentifier
    String name
    String otherIds
    String attributes
    String synonyms

    static hasMany = [variantStrains: VariantStrain, alleles: Allele]

    static constraints = {
        primaryIdentifier nullable: true
//        primaryIdentifier unique: true
        name nullable: false
        otherIds nullable: true
        attributes nullable: true
        synonyms nullable: true
    }

    static mapping = {
        name index:'name_idx'
//        primaryIdentifier index:'primary_identifier_idx'
        version false
    }
}
