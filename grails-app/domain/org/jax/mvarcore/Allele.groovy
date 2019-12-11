package org.jax.mvarcore

class Allele {

    String primaryIdentifier
    String symbol
    String name
    String type

    static hasMany = [strains: Strain]

    static constraints = {
        primaryIdentifier nullable: false
        primaryIdentifier unique: true
        symbol nullable: false
        name nullable: false
        type nullable: true
    }

    static mapping = {
        name index:'name_idx'
        primaryIdentifier index:'primary_identifier_idx'
        version false
    }

    static belongsTo = Strain
}
