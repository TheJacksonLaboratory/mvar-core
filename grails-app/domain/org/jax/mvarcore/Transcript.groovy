package org.jax.mvarcore


class Transcript {

    String primaryIdentifier
    int length
    Long locationStart
    Long locationEnd
    String mgiGeneIdentifier
    String chromosome
    String ensGeneIdentifier

    static mapping = {
        primaryIdentifier index: true
        mgiGeneIdentifier index: true
        ensGeneIdentifier index: true
        version false
    }

    static constraints = {
        primaryIdentifier nullable: false, unique: true
        length nullable: true
        locationStart nullable: false
        locationEnd nullable: false
        mgiGeneIdentifier nullable: true
        chromosome nullable: false
        ensGeneIdentifier nullable:true
    }
}
