package org.jax.mvarcore

class SequenceOntology {

    String accession
    String name
    String definition
    String parents
    String children

    static constraints = {
        accession nullable: false, unique: true
        name nullable: false
        definition nullable: true, sqlType: 'text'
        parents nullable: true
        children nullable: true
    }

    static mapping = {
        name index:'name_idx'
        version false
    }
}