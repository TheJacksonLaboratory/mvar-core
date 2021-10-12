package org.jax.mvarcore

class SequenceOntology {

    String soId
    String label
    String subClassOf
    String definition

    static constraints = {
        soId nullable: false, unique: true
        label nullable: false
        definition nullable: true, sqlType: 'text'
        subClassOf nullable: true
    }

    static mapping = {
        label index:'name_idx'
        version false
    }
}