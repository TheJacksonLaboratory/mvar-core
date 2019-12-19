package org.jax.mvarcore


class Transcript {

    String primaryIdentifier
    int length
    String chromosome
    Long locationStart
    Long locationEnd
    String mgiGeneIdentifier
    String ensGeneIdentifier

    static mapping = {
        primaryIdentifier index: true
        mgiGeneIdentifier index: true
        ensGeneIdentifier index: true
        version false
    }

    static constraints = {
        primaryIdentifier nullable: false
        primaryIdentifier unique: true
        length nullable: true
        chromosome nullable: false
        locationStart nullable: false
        locationEnd nullable: false
        mgiGeneIdentifier nullable: true
        ensGeneIdentifier nullable:true
    }
}
