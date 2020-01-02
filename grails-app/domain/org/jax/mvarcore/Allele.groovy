package org.jax.mvarcore

class Allele {

    String type
    String name
    String primaryIdentifier
    String symbol

    static hasMany = [strains: Strain]

    static constraints = {
        type nullable: true
        name nullable: false
        primaryIdentifier nullable: false, unique: true
        symbol nullable: false
    }

    static mapping = {
        name index:'name_idx'
        primaryIdentifier index:'primary_identifier_idx'
        version false
    }

    static belongsTo = Strain
}
